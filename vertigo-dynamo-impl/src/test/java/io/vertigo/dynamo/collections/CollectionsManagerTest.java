package io.vertigo.dynamo.collections;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.Function;
import io.vertigo.dynamo.TestUtil;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamock.domain.famille.Famille;
import io.vertigo.kernel.lang.Option;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author dchallas 
 * @version $Id: CollectionsManagerTest.java,v 1.5 2014/01/20 17:51:47 pchretien Exp $
 */
public class CollectionsManagerTest extends AbstractTestCaseJU4 {
	private static final String Ba = "Ba";
	private static final String aaa = "aaa";
	private static final String bb = "bb";
	private DtDefinition dtDefinitionFamille;
	@Inject
	private CollectionsManager collectionsManager;

	/** {@inheritDoc} */
	@Override
	protected void doSetUp() {
		dtDefinitionFamille = DtObjectUtil.findDtDefinition(Famille.class);
	}

	@Test
	public void testDescription() {
		TestUtil.testDescription(collectionsManager);
	}

	/**
	 * @see CollectionsManager#createSort(String, boolean, boolean, boolean)
	 */
	@Test
	public void testCreateSortState() {
		final Function<DtList<DtObject>, DtList<DtObject>> sortStateAsc = collectionsManager.createSort("LIBELLE", false, true, true);
		Assert.assertNotNull(sortStateAsc);
	}

	/**
	 * @see CollectionsManager#createSort(String, boolean, boolean, boolean)
	 */
	@Test
	public void testHeavySort() {
		// final DtList<Famille> sortDtc;
		final DtList<Famille> dtc = createFamilles();
		//
		for (int i = 0; i < 50000; i++) {
			final Famille mocka = new Famille();
			mocka.setLibelle(String.valueOf(i % 100));
			dtc.add(mocka);
		}
		final Function<DtList<Famille>, DtList<Famille>> sortState = collectionsManager.createSort("LIBELLE", false, true, true);
		final DtList<Famille> sortedDtc = sortState.apply(dtc);
		nop(sortedDtc);

	}

	/**
	 * @see CollectionsManager#createSort(String, boolean, boolean, boolean)
	 */
	@Test
	public void testSort() {
		DtList<Famille> sortDtc;
		final DtList<Famille> dtc = createFamilles();
		final String[] indexDtc = indexId(dtc);

		// Cas de base.
		// ======================== Ascendant
		// =================================== nullLast
		// ================================================ ignoreCase
		sortDtc = collectionsManager.<Famille> createSort("LIBELLE", false, true, true)//
				.apply(dtc);

		assertEquals(indexDtc, indexId(dtc));
		assertEquals(new String[] { aaa, Ba, bb, null }, indexId(sortDtc));

		// ======================== Ascendant
		// =================================== nullLast
		// ================================================ not ignoreCase
		sortDtc = collectionsManager.<Famille> createSort("LIBELLE", false, true, false)//
				.apply(dtc);

		assertEquals(indexDtc, indexId(dtc));
		assertEquals(new String[] { Ba, aaa, bb, null }, indexId(sortDtc));

		// ======================== Ascendant
		// =================================== not nullLast
		// ================================================ ignoreCase
		sortDtc = collectionsManager.<Famille> createSort("LIBELLE", false, false, true)//
				.apply(dtc);
		assertEquals(indexDtc, indexId(dtc));
		assertEquals(new String[] { null, aaa, Ba, bb }, indexId(sortDtc));

		// ======================== Ascendant
		// =================================== not nullLast
		// ================================================ not ignoreCase
		sortDtc = collectionsManager.<Famille> createSort("LIBELLE", false, false, false)//
				.apply(dtc);
		assertEquals(indexDtc, indexId(dtc));
		assertEquals(new String[] { null, Ba, aaa, bb }, indexId(sortDtc));

		// ======================== Descendant
		// =================================== nullLast
		// ================================================ ignoreCase
		sortDtc = collectionsManager.<Famille> createSort("LIBELLE", true, true, true)//
				.apply(dtc);
		assertEquals(indexDtc, indexId(dtc));
		assertEquals(new String[] { bb, Ba, aaa, null }, indexId(sortDtc));

		// ======================== Descendant
		// =================================== nullLast
		// ================================================ not ignoreCase
		sortDtc = collectionsManager.<Famille> createSort("LIBELLE", true, true, false)//
				.apply(dtc);
		assertEquals(indexDtc, indexId(dtc));
		assertEquals(new String[] { bb, aaa, Ba, null }, indexId(sortDtc));

		// ======================== Descendant
		// =================================== not nullLast
		// ================================================ ignoreCase
		sortDtc = collectionsManager.<Famille> createSort("LIBELLE", true, false, true)//
				.apply(dtc);
		assertEquals(indexDtc, indexId(dtc));
		assertEquals(new String[] { null, bb, Ba, aaa }, indexId(sortDtc));

		// ======================== Descendant
		// =================================== not nullLast
		// ============================================ not ignoreCase
		sortDtc = collectionsManager.<Famille> createSort("LIBELLE", true, false, false)//
				.apply(dtc);
		assertEquals(indexDtc, indexId(dtc));
		assertEquals(new String[] { null, bb, aaa, Ba }, indexId(sortDtc));
	}

	/**
	 * @see CollectionsManager#createFilterByValue
	 */
	@Test
	public void testCreateValueFilter() {
		final Function<DtList<Famille>, DtList<Famille>> filter = collectionsManager.createFilterByValue("LIBELLE", "a");
		Assert.assertNotNull(filter);
	}

	/**
	 * @see CollectionsManager#createFilterByTwoValues
	 */
	@Test
	public void testCreateTwoValuesFilter() {
		final Function<DtList<Famille>, DtList<Famille>> filter = collectionsManager.createFilterByTwoValues("LIBELLE", "a", "FAM_ID", 1L);
		Assert.assertNotNull(filter);
	}

	/**
	 * @see CollectionsManager#createFilter
	 */
	@Test
	public void testFilter() {
		final DtList<Famille> result = collectionsManager.<Famille> createFilterByValue("LIBELLE", "aaa")//
				.apply(createFamilles());
		Assert.assertEquals(1, result.size());
	}

	/**
	 * @see CollectionsManager#createFilterByTwoValues
	 */
	@Test
	public void testFilterTwoValues() {
		final DtList<Famille> result = collectionsManager.<Famille> createFilterByTwoValues("LIBELLE", "aaa", "FAM_ID", 13L)//
				.apply(createFamillesForRangeTest());
		Assert.assertEquals(1, result.size());
	}

	/**
	 * @see CollectionsManager#createFilter
	 */
	@Test
	public void testFilterFullText() {
		final DtList<Famille> result = collectionsManager.<Famille> createFilter("aa", 1000, dtDefinitionFamille.getFields())//
				.apply(createFamilles());
		Assert.assertEquals(1, result.size(), 0);

	}

	/**
	 * @see CollectionsManager#createFilter
	 */
	@Test
	public void testFilterFullTextTokenizer() {
		final DtList<Famille> dtc = createFamilles();
		final Collection<DtField> searchedDtFields = dtDefinitionFamille.getFields();
		final Famille mock1 = new Famille();
		mock1.setLibelle("Agence de l'Ouest");
		dtc.add(mock1);

		final Famille mock2 = new Famille();
		mock2.setLibelle("Hôpital et autres accents çava où ãpied");
		dtc.add(mock2);

		Assert.assertTrue("La recherche n'est pas case insensitive", filter(dtc, "agence", 1000, searchedDtFields).size() == 1);//majuscule/minuscule
		Assert.assertTrue("La recherche n'est pas plain text", filter(dtc, "l'ouest", 1000, searchedDtFields).size() == 1);//tokenizer
		Assert.assertTrue("La recherche ne supporte pas les accents", filter(dtc, "hopital", 1000, searchedDtFields).size() == 1);//accents
		Assert.assertTrue("La recherche ne supporte pas les caractères spéciaux fr (ç)", filter(dtc, "cava", 1000, searchedDtFields).size() == 1); //accents fr (ç)
		Assert.assertTrue("La recherche ne supporte pas les caractères spéciaux latin1 (ã)", filter(dtc, "apied", 1000, searchedDtFields).size() == 1); //accents autre (ã)
		Assert.assertTrue("La recherche ne supporte pas la recherche par préfix", filter(dtc, "apie", 1000, searchedDtFields).size() == 1);//prefix
	}

	private List<Famille> filter(final DtList<Famille> dtc, final String query, final int nbRows, final Collection<DtField> searchedDtFields) {
		return collectionsManager.<Famille> createFilter(query, nbRows, searchedDtFields)//
				.apply(dtc);
	}

	/**
	 * @see CollectionsManager#createFilter
	 */
	@Test
	public void testFilterFullTextElision() {
		final DtList<Famille> dtc = createFamilles();
		final Collection<DtField> searchedDtFields = dtDefinitionFamille.getFields();

		final Famille mock1 = new Famille();
		mock1.setLibelle("Agence de l'Ouest");
		dtc.add(mock1);

		final Famille mock2 = new Famille();
		mock2.setLibelle("Hôpital et autres accents çava où àpied");
		dtc.add(mock2);

		Assert.assertTrue("La recherche ne supporte pas l'elision", filter(dtc, "ouest", 1000, searchedDtFields).size() == 1);
	}

	/**
	 * @see CollectionsManager#createFilter
	 */
	@Test
	public void testFilterFullTextMultiKeyword() {
		final DtList<Famille> dtc = createFamilles();
		final Collection<DtField> searchedDtFields = dtDefinitionFamille.getFields();

		final Famille mock1 = new Famille();
		mock1.setLibelle("Agence de l'Ouest");
		dtc.add(mock1);

		final Famille mock2 = new Famille();
		mock2.setLibelle("Hôpital et autres accents çava où ãpied");
		dtc.add(mock2);

		Assert.assertTrue("La recherche ne supporte pas l'espace", filter(dtc, "agence de", 1000, searchedDtFields).size() == 1);//mots proches
		Assert.assertTrue("La recherche ne supporte pas l'utilisation de plusieurs mots", filter(dtc, "hopital accent", 1000, searchedDtFields).size() == 1);//mots séparés
		Assert.assertTrue("La recherche ne supporte pas l'inversion des mots", filter(dtc, "accent hopital", 1000, searchedDtFields).size() == 1);//inversés
		Assert.assertTrue("Les mots clés ne sont pas en 'ET'", filter(dtc, "agence hopital", 1000, searchedDtFields).size() == 0);//multi doc
	}

	/**
	 * Vérifie le comportement quand la recherche en commence par addresse trop de term du dictionnaire.
	 * Par défaut Lucene envoi une erreur TooMany...., le collectionsManager limite aux premiers terms.
	 * @see CollectionsManager#createFilter
	 */
	@Test
	public void testFilterFullTextBigList() {
		final Function<DtList<Famille>, DtList<Famille>> filter = collectionsManager.createFilter("a", 2000, dtDefinitionFamille.getFields());
		Assert.assertNotNull(filter);
		final DtList<Famille> bigFamillyList = new DtList<>(Famille.class);
		for (int i = 0; i < 50000; i++) {
			final Famille mocka = new Famille();
			mocka.setLibelle("blabla a" + (char) ('a' + i % 26) + String.valueOf(i % 100));
			bigFamillyList.add(mocka);
		}
		final DtList<Famille> result = filter.apply(bigFamillyList);
		Assert.assertEquals(2000, result.size(), 0);
	}

	/**
	 * @see CollectionsManager#createFilterSubList
	 */
	@Test
	public void testSubList() {
		// on test une implémentation de référence ArrayList
		final List<String> list = new ArrayList<>();
		list.add("a");
		list.add("b");
		Assert.assertEquals(0, list.subList(0, 0).size());
		Assert.assertEquals(2, list.subList(0, 2).size()); // >0, 1
		Assert.assertEquals(1, list.subList(1, 2).size()); // >1
		Assert.assertEquals(0, list.subList(2, 2).size());
		// on teste notre implémentation
		Assert.assertEquals(0, subList(createFamilles(), 0, 0).size());
		Assert.assertEquals(2, subList(createFamilles(), 0, 2).size());
		Assert.assertEquals(1, subList(createFamilles(), 1, 2).size());
		Assert.assertEquals(0, subList(createFamilles(), 2, 2).size());
	}

	private DtList<Famille> subList(final DtList<Famille> dtc, final int start, final int end) {
		return collectionsManager.<Famille> createFilterSubList(start, end)//
				.apply(dtc);
	}

	/**
	 * @see CollectionsManager#createFilterSubList
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testSubListFail1() {
		// On teste les dépassements.
		subList(createFamilles(), 5, 5);
		// "[Assertion.precondition] IndexOutOfBoundException....
	}

	/**
	 * @see CollectionsManager#createFilterSubList
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testSubListFail2() {
		// On teste les dépassements.
		subList(createFamilles(), 1, 20);
		// "[Assertion.precondition] IndexOutOfBoundException....
	}

	/**
	 * combiner sort/filter ; filter/sort ; sublist/sort ; filter/sublist.
	 * 
	 */
	@Test
	public void testChainFilterSortSubList() {

		final DtList<Famille> dtc = createFamilles();
		final String[] indexDtc = indexId(dtc);

		final Function<DtList<Famille>, DtList<Famille>> filter = collectionsManager.createFilterByValue("LIBELLE", "aaa");
		final Function<DtList<Famille>, DtList<Famille>> sortState = collectionsManager.createSort("LIBELLE", false, true, true);
		final int sizeDtc = dtc.size();

		DtList<Famille> sortDtc, filterDtc, subList;
		// ======================== sort/filter
		sortDtc = sortState.apply(dtc);
		assertEquals(new String[] { aaa, Ba, bb, null }, indexId(sortDtc));
		filterDtc = filter.apply(sortDtc);
		assertEquals(new String[] { aaa }, indexId(filterDtc));

		// ======================== sort/sublist
		sortDtc = sortState.apply(dtc);
		assertEquals(new String[] { aaa, Ba, bb, null }, indexId(sortDtc));
		subList = subList(sortDtc, 0, sizeDtc - 1);
		assertEquals(new String[] { aaa }, indexId(filterDtc));

		// ======================== filter/sort
		filterDtc = filter.apply(dtc);
		assertEquals(new String[] { aaa }, indexId(filterDtc));
		sortDtc = sortState.apply(filterDtc);
		assertEquals(new String[] { aaa }, indexId(filterDtc));

		// ======================== filter/sublist
		filterDtc = filter.apply(dtc);
		assertEquals(new String[] { aaa }, indexId(filterDtc));
		subList = subList(filterDtc, 0, filterDtc.size() - 1);
		assertEquals(new String[] { aaa }, indexId(filterDtc));

		// ======================== sublist/sort
		subList = subList(dtc, 0, sizeDtc - 1);
		assertEquals(new String[] { Ba, null, aaa }, indexId(subList));
		sortDtc = sortState.apply(subList);
		assertEquals(new String[] { aaa }, indexId(filterDtc));

		// ======================== sublist/filter
		subList = subList(dtc, 0, sizeDtc - 1);
		assertEquals(new String[] { Ba, null, aaa }, indexId(subList));
		filterDtc = filter.apply(subList);
		assertEquals(new String[] { aaa }, indexId(filterDtc));

		// === dtc non modifié
		assertEquals(indexDtc, indexId(dtc));

	}

	/**
	 * @see CollectionsManager#createFilter
	 */
	@Test
	public void testCreateFilterForValue() {
		final Function<DtList<Famille>, DtList<Famille>> filter = collectionsManager.createFilter(new ListFilter("LIBELLE" + ":\"aaa\""));
		Assert.assertNotNull(filter);
	}

	/**
	 * @see CollectionsManager#createFilter
	 */
	@Test
	public void testTermFilterString() {
		testTermFilter("LIBELLE:\"aaa\"", 3);
		testTermFilter("LIBELLE:\"aaab\"", 1);
	}

	/**
	 * @see CollectionsManager#createFilter
	 */
	@Test
	public void testTermFilterLong() {
		testTermFilter("FAM_ID:\"1\"", 1);
		testTermFilter("FAM_ID:\"11\"", 1);
		testTermFilter("FAM_ID:\"2\"", 0);
	}

	/**
	 * @see CollectionsManager#createFilterByRange
	 */
	@Test
	public void testCreateFilterByRange() {
		final Function<DtList<Famille>, DtList<Famille>> filter = collectionsManager.createFilterByRange("LIBELLE", Option.option("a"), Option.option("b"));
		Assert.assertNotNull(filter);
	}

	/**
	 * @see CollectionsManager#createFilterByRange
	 */
	@Test
	public void testCreateFilter() {
		final Function<DtList<Famille>, DtList<Famille>> filter = collectionsManager.createFilter(new ListFilter("LIBELLE" + ":[a TO b]"));
		Assert.assertNotNull(filter);
	}

	/**
	 * @see CollectionsManager#createFilter
	 */
	@Test
	public void testRangeFilter() {
		testRangeFilter("LIBELLE" + ":[a TO b]", 5);
	}

	/**
	 * @see CollectionsManager#createFilter
	 */
	@Test
	public void testRangeFilterLong() {
		testRangeFilter("FAM_ID:[1 TO 10]", 3);
		testRangeFilter("FAM_ID:[1 TO 10[", 2);
		testRangeFilter("FAM_ID:]1 TO 10]", 2);
		testRangeFilter("FAM_ID:]1 TO 10[", 1);
		testRangeFilter("FAM_ID:]1 TO *[", 5);
		testRangeFilter("FAM_ID:[* TO *[", 6);
	}

	/**
	 * @see CollectionsManager#createFilter
	 */
	@Test
	public void testRangeFilterString() {
		testRangeFilter("LIBELLE:[a TO b]", 5);
		testRangeFilter("LIBELLE:[* TO c[", 7);
		testRangeFilter("LIBELLE:[* TO c]", 8);
		testRangeFilter("LIBELLE:[* TO cb]", 9);
		testRangeFilter("LIBELLE:[aaab TO aaac]", 2);
		testRangeFilter("LIBELLE:[aaab TO aaac[", 1);
	}

	private void testTermFilter(final String filterString, final int countEspected) {
		final DtList<Famille> result = collectionsManager.<Famille> createFilter(new ListFilter(filterString))//
				.apply(createFamillesForRangeTest());
		Assert.assertEquals(countEspected, result.size());
	}

	private void testRangeFilter(final String filterString, final int countEspected) {
		final Function<DtList<Famille>, DtList<Famille>> filter = collectionsManager.createFilter(new ListFilter(filterString));
		Assert.assertNotNull(filter);
		final DtList<Famille> result = filter.apply(createFamillesForRangeTest());
		Assert.assertEquals(countEspected, result.size());
	}

	private DtList<Famille> createFamillesForRangeTest() {
		final DtList<Famille> dtc = createFamilles();

		final Famille mock1 = new Famille();
		mock1.setFamId(1L);
		mock1.setLibelle("aaab");
		dtc.add(mock1);

		final Famille mock2 = new Famille();
		mock2.setFamId(10L);
		mock2.setLibelle("aaac");
		dtc.add(mock2);

		final Famille mock3 = new Famille();
		mock3.setFamId(11L);
		mock3.setLibelle("caaa");
		dtc.add(mock3);

		final Famille mock4 = new Famille();
		mock4.setFamId(12L);
		mock4.setLibelle("aaa");
		dtc.add(mock4);

		final Famille mock5 = new Famille();
		mock5.setFamId(13L);
		mock5.setLibelle("aaa");
		dtc.add(mock5);

		final Famille mock6 = new Famille();
		mock6.setFamId(3L);
		mock6.setLibelle("c");
		dtc.add(mock6);

		return dtc;
	}

	// -------------- PRIVATE
	/**
	 * Asserts that two booleans are equal.
	 * 
	 */
	private void assertEquals(final String[] expected, final String[] actual) {
		Assert.assertEquals(Arrays.toString(expected), Arrays.toString(actual));
	}

	private static String[] indexId(final DtList<Famille> dtc) {
		final String[] index = new String[dtc.size()];
		for (int i = 0; i < dtc.size(); i++) {
			index[i] = dtc.get(i).getLibelle();
		}
		return index;
	}

	private DtList<Famille> createFamilles() {
		final DtList<Famille> dtc = new DtList<>(Famille.class);
		// les index sont données par ordre alpha > null à la fin >
		final Famille mockB = new Famille();
		mockB.setLibelle(Ba);
		dtc.add(mockB);

		final Famille mockNull = new Famille();
		// On ne renseigne pas le libelle > null
		dtc.add(mockNull);

		final Famille mocka = new Famille();
		mocka.setLibelle(aaa);
		dtc.add(mocka);

		final Famille mockb = new Famille();
		mockb.setLibelle(bb);
		dtc.add(mockb);

		// On crée et on supprimme un élément dans la liste pour vérifier
		// l'intégrité de la liste (Par rapport aux null).
		final Famille mockRemoved = new Famille();
		mockRemoved.setLibelle("mockRemoved");
		dtc.add(mockRemoved);

		dtc.remove(mockRemoved);
		return dtc;
	}
}