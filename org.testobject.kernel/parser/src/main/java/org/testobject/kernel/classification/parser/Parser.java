package org.testobject.kernel.classification.parser;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.collections.Sets;
import org.testobject.commons.util.functional.Functions;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.tree.r.RTree;
import org.testobject.kernel.api.classification.classifiers.Classifier;
import org.testobject.kernel.api.classification.graph.Node;
import org.testobject.kernel.classification.graph.Optimizer;
import org.testobject.kernel.classification.parser.Cache.IsCache;
import org.testobject.kernel.classification.parser.Operations.Input;
import org.testobject.kernel.classification.parser.Operations.Map;
import org.testobject.kernel.classification.parser.Operations.Operation;

/**
 * passes
 * 
 *      - low-contrast
 *         - auto-contrast -> quantize to 16 colors -> linear blob fill
 *         
 *      - high-contrast
 *         - graph-based segmentation 
 * 
 * 
 * multi-pass scan
 * 
 *    (0) labels
 *       
 *        - label "hierarchy" starts complete empty
 *        - hierarchy = simple set of bounding boxes, not a hierarchical structure
 *        - R-tree
 * 
 *    (1) high-contrast
 *    
 *        - low-level classifiers (icon, text) label segments with probability
 *        - add potentially overlapping labels to hierarchy
 *    
 *    (2) low-contrast
 *    
 *        - high-level classifiers (buttons, textboxes) look for required low-level proposals
 *        - probability of high-level elements is a combination of low-level proposal and high-level probability
 * 
 * graphs
 *    
 *     - render-graph
 *          - procedural description of ui elements
 *          - with free variables
 *          
 *     - label-graph
 *          - keeps elements with labels (proposals of classifiers)
 *          
 *     - locator-graph
 *          - contains compact query-like recursive ui locators
 *          - e.g. for button: 
 *                   button (feature shape as render-graph, feature contour)
 *                        text (feature char candidate)
 * 
 * @author enijkamp
 * 
 */
public interface Parser {

	// plan
	class Builder {

		static class PlanImpl implements Plan {

			private final String qualifier;
			private final List<Operation> operations;
			private final List<Cache.IsCache<?>> caches;

			public PlanImpl(String qualifier, List<Operation> operations, List<IsCache<?>> caches) {
				this.qualifier = qualifier;
				this.operations = operations;
				this.caches = caches;
			}

			@Override
			public List<Operation> getOperations() {
				return operations;
			}

			@Override
			public List<IsCache<?>> getCaches() {
				return caches;
			}

			@Override
			public String getQualifier() {
				return qualifier;
			}

		}

		static class PlanBuilderImpl implements Plan.Builder {

			private final String qualifier;
			private final List<Operation> operations = Lists.newLinkedList();
			private final List<Cache.IsCache<?>> caches = Lists.newLinkedList();

			public PlanBuilderImpl(String qualifier) {
				this.qualifier = qualifier;
			}

			@Override
			public Plan.Builder caches(Operation... operations) {
				// sanity
				for (Operation operation : operations) {
					if (operation instanceof Cache.IsCache == false) {
						throw new IllegalArgumentException("operation '" + operation + "' is not a cache");
					}
				}

				// add
				for (Operation operation : operations) {
					Cache.IsCache<?> cache = (Cache.IsCache<?>) operation;
					this.caches.add(cache);
				}

				return this;
			}

			@Override
			public Plan.Builder input(Input input) {
				this.operations.add(input);

				return this;
			}

			@Override
			public <From, To> Plan.Builder map(Operations.Map<From, To> map) {
				this.operations.add(map);

				return this;
			}

			@Override
			public <From, To> Plan.Builder reduce(Operations.Reduce<From, To> reduce) {
				this.operations.add(reduce);

				return this;
			}

			@Override
			public Plan.Builder map(Plan.Builder... parallel) {
				List<Plan> childs = Lists.newArrayList(parallel.length);
				for (Plan.Builder child : parallel) {
					childs.add(child.build());
				}

				operations.add(new Parallel(childs));

				return this;
			}

			@Override
			public Plan build() {
				return new PlanImpl(this.qualifier, this.operations, this.caches);
			}
		}

		public static Plan.Builder plan(String qualifier) {
			return new PlanBuilderImpl(qualifier);
		}

		// qualifiers
		public static class Naming {

			private int stage, pass;

			private Naming() {

			}

			public static Naming create() {
				return new Naming();
			}

			public String stage() {
				return "stage " + Integer.toString(stage++);
			}

			public String pass() {
				return "pass " + Integer.toString(pass++);
			}
		}

		// map / reduce
		public static Input cache(Input input) {
			return new Cache.Input(input);
		}

		public static Map<Node, Node> cache(Map<Node, Node> map) {
			return new Cache.Map(map);
		}

		public static Input segment(float sigma, double threshold) {
			return new Segmentation(new Segmenter.Graph(sigma, threshold));
		}

		public static Input segment(float sigma, double threshold, int minSize) {
			return new Segmentation(new Segmenter.Graph(sigma, threshold, minSize));
		}

		public static Map<Node, Node> group() {
			return new Grouping.Fat(Grouping.Fat.Filter.SMALL);
		}

		public static Classification classify(Classifier... classifiers) {
			return new Classification(Arrays.asList(classifiers));
		}

		public static Map<Node, Node> prune() {
			return new Pruning.LowPass();
		}

		public static Locators locators() {
			return new Locators();
		}
		
		public static Operations.Map<Node, Node> expandFix() {
			return new ExpandFix();
		}

		public static ZLevelFix zFix() {
			return new ZLevelFix();
		}

		public enum Contestants {
			flat, hierarchical
		}

		public static Optimization optimize(Map<Node, Node> map, Contestants contestants) {
			return optimize(lowpass(), map, contestants);
		}

		public static Optimization optimize(final Optimizer<Node, Node> optimizer, final Map<Node, Node> map, Contestants contestants) {
			if (map instanceof Cache.IsCache == false) {
				throw new IllegalArgumentException();
			}

			return optimize(optimizer, new Functions.Function0<Node>() {
				@Override
				public Node apply() {
					@SuppressWarnings("unchecked")
					Cache.IsCache<Node> cache = (Cache.IsCache<Node>) map;
					return cache.getCache();
				}
			}, contestants);
		}

		public static Optimization optimize(Optimizer<Node, Node> optimizer, Functions.Function0<Node> lowpass, Contestants contestants) {
			if (contestants == Contestants.hierarchical) {
				return new Optimization.Hierarchical(optimizer, lowpass);
			} else {
				return new Optimization.Flat(optimizer, lowpass);
			}
		}

		public enum Breeding {
			winners, loosers
		}

		public static Optimizer<Node, Node> lowpass() {
			return lowpass(Breeding.winners);
		}

		public static Optimizer<Node, Node> lowpass(Breeding breeding) {

			Optimizer.BoxAdapter<Node> boxAdapter = new Optimizer.BoxAdapter<Node>() {
				@Override
				public Rectangle.Double getBoundingBox(Node node) {
					return toDoubleRect(node.getElement().getBoundingBox());
				}

				private Rectangle.Double toDoubleRect(Rectangle.Int rect) {
					return new Rectangle.Double(rect.x, rect.y, rect.w, rect.h);
				}
			};

			Optimizer.LabelAdapter<Node> labelAdapter = new Optimizer.LabelAdapter<Node>() {
				@Override
				public Rectangle.Double getBoundingBox(Node node) {
					return toDoubleRect(node.getElement().getBoundingBox());
				}

				private Rectangle.Double toDoubleRect(Rectangle.Int rect) {
					return new Rectangle.Double(rect.x, rect.y, rect.w, rect.h);
				}

				@Override
				public double getProbability(Node contestant) {
					return contestant.getElement().getLabel().getLikelihood().photometric();
				}
			};

			if (breeding == Breeding.loosers) {

				return new Optimizer.LowPassBreedLoosers<Node>(labelAdapter, new RTree.Factory<Node>());

			} else {

				return new Optimizer.LowPassBreedWinners<Node, Node>(boxAdapter, labelAdapter, new RTree.Factory<Node>());
			}
		}

		// tie proxy
		public static Input tie(final Input proxee, final Functions.VoidFunction1<Node> callback) {
			return new Input() {
				@Override
				public Node apply(Image.Int raw) {
					Node result = proxee.apply(raw);
					callback.apply(result);
					return result;
				}

				@Override
				public String toString() {
					return proxee.toString();
				}
			};
		}

		public static <From, To> Operations.Map<From, To> tie(final Operations.Map<From, To> proxee,
				final Functions.VoidFunction1<To> callback) {
			return new Operations.Map<From, To>() {
				@Override
				public To apply(From node, Context context) {
					To result = proxee.apply(node, context);
					callback.apply(result);
					return result;
				}

				@Override
				public String toString() {
					return proxee.toString();
				}
			};
		}

		public static <From, To> Operations.Reduce<From, To> tie(final Operations.Reduce<From, To> proxee,
				final Functions.VoidFunction1<To> callback) {
			return new Operations.Reduce<From, To>() {
				@Override
				public To apply(List<From> node, Context context) {
					To result = proxee.apply(node, context);
					callback.apply(result);
					return result;
				}

				@Override
				public String toString() {
					return proxee.toString();
				}
			};
		}
	}

	class Executor<To> {

		public interface Factory<To> {

			Executor<To> create();

		}

		public interface Progress {

			void begin(List<Cache.IsCache<?>> caches);

			void end(List<Cache.IsCache<?>> caches);

			void begin(Plan plan);

			void end(Plan plan, List<Object> result);

			void begin(Operation operation);

			void end(Operation operation, List<Object> result);

			class Builder {
				public static Progress none() {
					return new Progress() {
						@Override
						public void begin(List<IsCache<?>> caches) {

						}

						@Override
						public void end(List<IsCache<?>> caches) {

						}

						@Override
						public void begin(Plan plan) {

						}

						@Override
						public void end(Plan plan, List<Object> result) {

						}

						@Override
						public void begin(Operation operation) {

						}

						@Override
						public void end(Operation operation, List<Object> result) {

						}
					};
				}

				public static Progress sysout() {
					return new Progress() {
						private int indentPlan = 0;

						@Override
						public void begin(List<IsCache<?>> caches) {
							System.out.println(spaces(indentPlan) + "caches(" + caches.size() + ")");
						}

						@Override
						public void end(List<IsCache<?>> caches) {}

						@Override
						public void begin(Plan plan) {
							System.out.println(spaces(indentPlan) + "plan(" + plan.getQualifier() + ")");
							indentPlan += 2;
						}

						@Override
						public void end(Plan plan, List<Object> result) {
							indentPlan -= 2;
						}

						@Override
						public void begin(Operation operation) {
							System.out.println(spaces(indentPlan) + operation.toString());
							if (operation instanceof Parallel) {
								indentPlan += 2;
							}
						}

						@Override
						public void end(Operation operation, List<Object> result) {
							if (operation instanceof Parallel) {
								indentPlan -= 2;
							}
						}
					};
				}

				private static String spaces(int indent) {
					StringBuilder builder = new StringBuilder();
					for (int i = 0; i < indent; i++) {
						builder.append(" ");
					}
					return builder.toString();
				}
			}

		}

		private final Progress progress;
		private final Plan[] plans;

		public Executor(Progress progress, Plan... plans) {
			checkCaches(plans);
			this.progress = progress;
			this.plans = plans;
		}

		private void checkCaches(Plan[] plans) {

			Set<Cache.IsCache<?>> caches = Sets.newIdentitySet();
			Set<Cache.IsCache<?>> clear = Sets.newIdentitySet();

			for (Plan plan : plans) {
				for (Operations.Operation operation : plan.getOperations()) {
					if (operation instanceof Cache.IsCache) {
						Cache.IsCache<?> cache = (Cache.IsCache<?>) operation;
						caches.add(cache);
					}
				}

				clear.addAll(plan.getCaches());
			}

			caches.removeAll(clear);

			if (caches.isEmpty() == false) {
				String names = "";
				for (Cache.IsCache<?> cache : caches) {
					names += cache.toString() + " ";
				}
				throw new IllegalStateException("you've missed some caches: " + names);
			}
		}

		public List<To> execute(Image.Int raw) {

			List<To> result = Lists.empty();

			for (Plan plan : plans) {
				result = execute(raw, plan);
			}

			return result;
		}

		private List<To> execute(Image.Int raw, Plan plan) {

			List<Object> nodes = Lists.newLinkedList();

			progress.begin(plan);
			{
				// clear cache
				{
					progress.begin(plan.getCaches());
					{
						for (Cache.IsCache<?> cache : plan.getCaches()) {
							cache.reset();
						}
					}
					progress.end(plan.getCaches());
				}

				// run operations
				{
					for (Operation operation : plan.getOperations()) {
						progress.begin(operation);
						{
							nodes = execute(raw, nodes, operation);
						}
						progress.end(operation, nodes);
					}
				}

			}
			progress.end(plan, nodes);

			return Lists.<To> castList(nodes);
		}

		@SuppressWarnings("unchecked")
		private List<Object> execute(Image.Int raw, List<Object> nodes, Operation operation) {

			if (operation instanceof Input) {
				Input input = (Input) operation;
				return Lists.<Object> toList(input.apply(raw));
			}

			if (operation instanceof Operations.Map) {
				assertSize(nodes, 1);
				Operations.Map<Object, Object> map = (Operations.Map<Object, Object>) operation;
				return Lists.toList(map.apply(nodes.get(0), Context.Factory.create(raw)));
			}

			if (operation instanceof Operations.Reduce) {
				Operations.Reduce<Object, Object> reduce = (Operations.Reduce<Object, Object>) operation;
				return Lists.toList(reduce.apply(nodes, Context.Factory.create(raw)));
			}

			if (operation instanceof Parallel) {
				Parallel parallel = (Parallel) operation;
				List<Object> result = Lists.newArrayList(parallel.getPlans().size());
				for (Plan plan : parallel.getPlans()) {
					result.addAll(execute(raw, plan));
				}
				return result;
			}

			throw new IllegalArgumentException(operation.getClass().getSimpleName());
		}

		private void assertSize(List<?> nodes, int n) {
			if (nodes.size() != n) {
				throw new IllegalStateException("expect(" + Integer.toString(n) + ") != is(" + nodes.size() + ")");
			}
		}
	}
}
