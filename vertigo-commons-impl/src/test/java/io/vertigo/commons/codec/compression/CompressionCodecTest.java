/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.commons.codec.compression;

import io.vertigo.commons.codec.AbstractCodecTest;
import io.vertigo.commons.codec.Codec;
import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.impl.codec.compression.CompressionCodec;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test du codec de compresion.
 * 
 * @author pchretien
 */
public final class CompressionCodecTest extends AbstractCodecTest<byte[], byte[]> {
	/** {@inheritDoc} */
	@Override
	public Codec<byte[], byte[]> obtainCodec(final CodecManager codecManager) {
		return codecManager.getCompressionCodec();
	}

	/**
	 * Test des mécanismes de compression/décompression des valeurs null.
	 ** 
	 */
	@Override
	@Test
	public void testNull() {
		Assert.assertNull(codec.encode(null));
		Assert.assertNull(codec.decode(null));
	}

	/**
	 * Test des mécanismes de compression/décompression.
	 ** 
	 */
	@Override
	@Test
	public void testEncode() {
		Assert.assertNotNull(codec.encode(TEXT.getBytes()));

	}

	/** {@inheritDoc} */
	@Override
	@Test
	public void testDecode() throws Exception {
		final byte[] encodedValue = codec.encode(TEXT.getBytes());
		Assert.assertEquals(TEXT, new String(codec.decode(encodedValue)));

	}

	@Test
	public void testUncompressedDecode() {
		// object ne correspondant pas à une classe;
		final byte[] s = "qdfsdf".getBytes();
		Assert.assertTrue(s.length < CompressionCodec.MIN_SIZE_FOR_COMPRESSION);
		final byte[] result = codec.decode(s);
		Assert.assertEquals("qdfsdf", new String(result));
	}

	@Test
	public void testNopDecode() {
		// object sans préfixe de compression, est laissé tel quel;
		final byte[] s = "qdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdf".getBytes();
		Assert.assertTrue(s.length > CompressionCodec.MIN_SIZE_FOR_COMPRESSION);
		final byte[] result = codec.decode(s);
		Assert.assertEquals(new String(s), new String(result));
	}

	/** {@inheritDoc} */
	@Override
	@Test(expected = RuntimeException.class)
	public void testFailDecode() throws Exception {
		// object avec prefix ne correspondant pas à une classe;
		final byte[] s = "COMPqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdf".getBytes();
		Assert.assertTrue(s.length > CompressionCodec.MIN_SIZE_FOR_COMPRESSION);
		/* final byte[] result = */

		//Le decodage lance une exception
		codec.decode(s);
		Assert.fail();
	}
}
