package org.testobject.kernel.script;

import static org.junit.Assert.assertEquals;
import static org.testobject.kernel.script.Script.Builder.appears;
import static org.testobject.kernel.script.Script.Builder.at;
import static org.testobject.kernel.script.Script.Builder.button;
import static org.testobject.kernel.script.Script.Builder.click;
import static org.testobject.kernel.script.Script.Builder.disappears;
import static org.testobject.kernel.script.Script.Builder.images;
import static org.testobject.kernel.script.Script.Builder.path;
import static org.testobject.kernel.script.Script.Builder.popup;
import static org.testobject.kernel.script.Script.Builder.textBox;

import java.io.IOException;

import org.junit.Test;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.kernel.replay.Playback;
import org.testobject.kernel.script.ScriptExecutor.Result;

public class TwitterReplayTest {

	@Test
	public void testWriteAndCancelMock() {
		testWriteAndCancel(new MockLevelReplayExecutor());
	}

	private void testWriteAndCancel(ScriptExecutor executor) {
		final Image.Int[] images =
		{
				read("1.png"),
				read("2.png"),
				read("3.png"),
				read("4.png"),
				read("5.png")
		};

		Script script =
				new Script.Builder()
						.step(
								images(images[0], images[1]),
								click(at(420, 60), path(button("write"))),
								disappears(path(button("write"))),
								appears(path(popup(textBox("")))))
						.step(
								images(images[1], images[2]),
								click(at(200, 200), path(popup(textBox("")))))
						.step(
								images(images[2], images[3]),
								click(at(30, 70), path(popup(button("cancel")))),
								disappears(path(popup(textBox("Heililo")))), // FIXME ocr should return "Hello" (en)
								appears(path(button("Discard"))))
						.build();

		Result result = executor.replay(script);
		assertEquals(result.getMessage(), Playback.Matcher.Match.SUCCESS, result.getMatch());
	}

	private static Image.Int read(String file) {
		try {
			return ImageUtil.read("android/4_0_3/replay/twitter/" + file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
