package io.vertigo.commons.codec.hash;

import io.vertigo.commons.codec.AbstractEncoderTest;
import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.codec.Encoder;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test l'encodeur de Hash.
 * @author pchretien
 * $Id: HashCodecSha1Test.java,v 1.4 2013/11/15 15:50:56 pchretien Exp $
 */
public final class HashCodecSha1Test extends AbstractEncoderTest<Encoder<byte[], byte[]>, byte[], byte[]> {

	private static final String ENCODE_TEXT = "cfc0d098e741c902a1dde49adef47cc2326af1cc";

	/** {@inheritDoc} */
	@Override
	public Encoder<byte[], byte[]> obtainCodec(final CodecManager codecManager) {
		return codecManager.getSha1Encoder();
	}

	/** {@inheritDoc} */
	@Override
	@Test(expected = NullPointerException.class)
	public void testNull() {
		/*
		 * Test de cr�ation de l'empreinte SHA 1.
		 * On v�rifie que null ne respecte pas le contrat.
		 */
		codec.encode(null);
	}

	/** {@inheritDoc} */
	@Override
	@Test
	public void testEncode() {
		//Carcat�res simples sans encodage
		final Encoder<byte[], String> hexEncoder = getCodecManager().getHexEncoder();
		final byte[] encoded = codec.encode(TEXT.getBytes());
		Assert.assertEquals(160 / 8, codec.encode(TEXT.getBytes()).length);

		Assert.assertEquals(ENCODE_TEXT, hexEncoder.encode(encoded));
	}
}