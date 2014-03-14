package io.vertigo.kernel.home.definitionspace;

import io.vertigo.AbstractTestCase2JU4;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.di.configurator.ComponentSpaceConfigBuilder;
import io.vertigo.kernel.metamodel.Definition;
import io.vertigo.kernel.metamodel.DefinitionReference;
import io.vertigo.kernel.metamodel.DefinitionUtil;
import io.vertigo.kernel.metamodel.Prefix;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Assert;
import org.junit.Test;

public class DefinitionSpaceTest extends AbstractTestCase2JU4 {

	@Override
	protected void configMe(final ComponentSpaceConfigBuilder componentSpaceConfiguilder) {
		componentSpaceConfiguilder//
				.withParam("log4j.configurationFileName", "/log4j.xml")//
				.withSilence(false);
	}

	@Test
	public void testEmpty() {
		Assert.assertEquals("definitionSpace must be emmpty", 0L, Home.getDefinitionSpace().getAllTypes().size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRegisterIsMandatory() {
		Assert.assertEquals("definitionSpace must be emmpty", 0L, Home.getDefinitionSpace().getAllTypes().size());
		Home.getDefinitionSpace().put(new SampleDefinition(), SampleDefinition.class);
	}

	@Test
	public void testRegister() throws IOException, ClassNotFoundException {
		Assert.assertEquals("definitionSpace must be emmpty", 0L, Home.getDefinitionSpace().getAllTypes().size());
		Home.getDefinitionSpace().register(SampleDefinition.class);
		Home.getDefinitionSpace().put(new SampleDefinition(), SampleDefinition.class);

		Assert.assertEquals("definitionSpace must contain one element ", 1L, Home.getDefinitionSpace().getAllTypes().size());
		Assert.assertEquals("definitionSpace[SampleDefinition.class] must contain one element ", 1L, Home.getDefinitionSpace().getAll(SampleDefinition.class).size());

		SampleDefinition sampleDefinition = Home.getDefinitionSpace().resolve("SAMPLE_THE_DEFINITION", SampleDefinition.class);
		Assert.assertNotNull(sampleDefinition);
		Assert.assertEquals("localName must be THE_DEFINITION", "THE_DEFINITION", DefinitionUtil.getLocalName(sampleDefinition.getName(), SampleDefinition.class));
		Assert.assertEquals("localName must be THE_DEFINITION", sampleDefinition.getName(), DefinitionUtil.getPrefix(SampleDefinition.class) + "_" + DefinitionUtil.getLocalName(sampleDefinition.getName(), SampleDefinition.class));

		DefinitionReference<SampleDefinition> sampleDefinitionRef = new DefinitionReference<>(sampleDefinition);

		byte[] serialized;
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
				oos.writeObject(sampleDefinitionRef);
				oos.flush();
			}
			serialized = bos.toByteArray();
		}

		//---
		DefinitionReference definitionReference;
		try (ByteArrayInputStream bis = new ByteArrayInputStream(serialized)) {
			try (ObjectInputStream ios = new ObjectInputStream(bis)) {
				definitionReference = DefinitionReference.class.cast(ios.readObject());
			}
		}

		Assert.assertNotSame("DefinitionReferences must be not strictly equals", sampleDefinitionRef, definitionReference);
		Assert.assertSame("Definitions must be strictly equals", sampleDefinition, definitionReference.get());
	}

	@Prefix("SAMPLE")
	public static class SampleDefinition implements Definition {

		public String getName() {
			return "SAMPLE_THE_DEFINITION";
		}
	}
}