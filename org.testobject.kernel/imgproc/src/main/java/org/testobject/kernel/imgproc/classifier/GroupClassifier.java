package org.testobject.kernel.imgproc.classifier;

import java.util.ArrayList;
import java.util.List;

import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.blob.Group;
import org.testobject.kernel.imgproc.blob.GroupBuilder;
import org.testobject.kernel.imgproc.blob.Mutator;

/**
 * 
 * @author enijkamp
 * 
 */
public class GroupClassifier extends ClassifierBase {
	private final static int FAT_X = 8, FAT_Y = 8;

	@Override
	public Specification getSpec() {
		return spec().requires(none()).returns(Classes.Group.class).build();
	}

	@Override
	public Match match(Context context, Blob blob) {
		// constraint 1: childs
		if (blob.children.isEmpty())
			return failed;

		// constraint 2: groups
		// merge horizontally
		List<Group<Blob>> groups = new GroupBuilder<Blob>().buildGroups(blob.children, FAT_X, FAT_Y);

		// no groups found
		boolean empty = true;
		for (Group<Blob> group : groups) {
			if (group.getContent().size() > 1) {
				empty = false;
			}
		}
		if (groups.isEmpty() || empty)
			return failed;

		// merge nodes
		List<Mutator.Mutation> mutations = new ArrayList<Mutator.Mutation>();
		for (Group<Blob> group : groups) {
			if (group.getContent().size() > 1) {
				mutations.add(new Mutator.CreateGroup(blob, group, new Classes.Group(group.getContent())));
			}
		}

		return new Match(new Classes.Group(), 1.0f, mutations);
	}

}