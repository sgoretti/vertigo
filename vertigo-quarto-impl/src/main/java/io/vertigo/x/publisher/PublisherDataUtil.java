package io.vertigo.x.publisher;

import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.DtProperty;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.StringUtil;
import io.vertigo.publisher.metamodel.PublisherField;
import io.vertigo.publisher.metamodel.PublisherNodeDefinition;
import io.vertigo.publisher.model.PublisherNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe de r�cup�ration des donn�es pour les editions.
 * @version $Id: PublisherDataUtil.java,v 1.8 2014/06/26 12:32:39 npiedeloup Exp $
 *
 * @author oboitel, npiedeloup
 */
public final class PublisherDataUtil {
	/**
	 * Constructeur priv� pour class utilitaire.
	 */
	private PublisherDataUtil() {
		//rien
	}

	/**
	 * Peuple un champs de type data dans un node.
	 * @param parentNode Node parent
	 * @param fieldName Nom du champs
	 * @param dtoValue Dto contenant les valeurs
	 * @return publisherNode du champ
	 */
	public static PublisherNode populateField(final PublisherNode parentNode, final String fieldName, final DtObject dtoValue) {
		final PublisherNode childNode = parentNode.createNode(fieldName);
		PublisherDataUtil.populateData(dtoValue, childNode);
		parentNode.setNode(fieldName, childNode);
		return childNode;
	}

	/**
	 * Peuple un champs de type data dans un node.
	 * @param parentNode Node parent
	 * @param fieldName Nom du champs
	 * @param dtcValue DTC contenant les valeurs
	 */
	public static void populateField(final PublisherNode parentNode, final String fieldName, final DtList<?> dtcValue) {
		final List<PublisherNode> publisherNodes = new ArrayList<>();
		for (final DtObject dto : dtcValue) {
			final PublisherNode childNode = parentNode.createNode(fieldName);
			PublisherDataUtil.populateData(dto, childNode);
			publisherNodes.add(childNode);
		}
		parentNode.setNodes(fieldName, publisherNodes);
	}

	/**
	 * Peuple un publisherDataNode � partir de champs du Dto qui correspondent.
	 * @param dto Objet de donn�es
	 * @param publisherDataNode PublisherDataNode
	 */
	public static void populateData(final DtObject dto, final PublisherNode publisherDataNode) {
		Assertion.checkNotNull(dto);
		Assertion.checkNotNull(publisherDataNode);
		//---------------------------------------------------------------------
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		final List<String> dtFieldNames = getDtFieldList(dtDefinition);
		final PublisherNodeDefinition pnDefinition = publisherDataNode.getNodeDefinition();
		int nbMappedField = 0;
		for (final PublisherField publisherField : pnDefinition.getFields()) {
			final String fieldName = publisherField.getName();
			if (!dtFieldNames.contains(fieldName)) {
				continue;
			}
			final DtField dtField = dtDefinition.getField(fieldName);
			final Object value = dtField.getDataAccessor().getValue(dto);
			nbMappedField++;
			switch (publisherField.getFieldType()) {
				case Boolean:
					Assertion.checkArgument(value instanceof Boolean, "Le champ {0} du DT {1} doit �tre un Boolean (non null)", fieldName, dtDefinition.getName());
					publisherDataNode.setBoolean(fieldName, (Boolean) value);
					break;
				case String:
					final String renderedField = value != null ? renderStringField(dto, dtField) : "";//un champ null apparait comme vide
					publisherDataNode.setString(fieldName, renderedField);
					break;
				case List:
					if (value != null) { //on autorise les listes null et on la traite comme vide 
						//car la composition d'objet m�tier n'est pas obligatoire 
						//et le champ sera peut �tre peupl� plus tard
						final DtList<?> dtc = (DtList<?>) value;
						final List<PublisherNode> publisherNodes = new ArrayList<>();
						for (final DtObject element : dtc) {
							final PublisherNode publisherNode = publisherDataNode.createNode(fieldName);
							populateData(element, publisherNode);
							publisherNodes.add(publisherNode);
						}
						publisherDataNode.setNodes(fieldName, publisherNodes);
					}
					break;
				case Node:
					if (value != null) { //on autorise les objet null, 
						//car la composition d'objet m�tier n'est pas obligatoire
						//et le champ sera peut �tre peupl� plus tard
						final DtObject element = (DtObject) value;
						final PublisherNode elementPublisherDataNode = publisherDataNode.createNode(fieldName);
						populateData(element, elementPublisherDataNode);
						publisherDataNode.setNode(fieldName, elementPublisherDataNode);
					}
					break;
				default:
					throw new IllegalArgumentException("Type non g�r� : " + publisherField.getFieldType());
			}
			//} else {
			//	Assertion.precondition(!(value instanceof Boolean), "Le champ {0} du DT {1} est un Boolean, et il ne doit pas �tre null", fieldName, dtDefinition.toURN());
			//-----------------------------------------------------------
			//	value = ""; //un champ null apparait comme vide
			//}
		}
		Assertion.checkState(nbMappedField > 0, "Aucun champ du Dt ne correspond � ceux du PublisherNode, v�rifier vos d�finitions. ({0}:{1}) et ({2}:{3})", "PN", pnDefinition.getFields(), dtDefinition.getName(), dtFieldNames);
	}

	/**
	 * G�re le rendu d'un champs de type String.
	 * @param dto l'objet sur lequel porte le champs
	 * @param dtField le champs � rendre
	 * @return la chaine de caract�re correspondant au rendu du champs
	 */
	public static String renderStringField(final DtObject dto, final DtField dtField) {
		final String unit = dtField.getDomain().getProperties().getValue(DtProperty.UNIT);

		final Object value = dtField.getDataAccessor().getValue(dto);
		final String formattedValue = dtField.getDomain().getFormatter().valueToString(value, dtField.getDomain().getDataType());
		return formattedValue + (!StringUtil.isEmpty(unit) ? " " + unit : "");
	}

	private static List<String> getDtFieldList(final DtDefinition dtDefinition) {
		final List<String> dtFieldNames = new ArrayList<>();
		for (final DtField dtField : dtDefinition.getFields()) {
			dtFieldNames.add(dtField.getName());
		}
		return dtFieldNames;
	}

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	//- G�n�ration de PublisherNode en KSP � partir des DtDefinitions
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	/**
	 * M�thode utilitaire pour g�n�rer une proposition de d�finition de PublisherNode, pour des DtDefinitions.
	 * @param dtDefinitions DtDefinition � utiliser.
	 * @return Proposition de PublisherNode.
	 */
	public static String generatePublisherNodeDefinitionAsKsp(final String... dtDefinitions) {
		final StringBuilder sb = new StringBuilder();
		for (final String dtDefinitionUrn : dtDefinitions) {
			appendPublisherNodeDefinition(sb, Home.getDefinitionSpace().resolve(dtDefinitionUrn, DtDefinition.class));
			sb.append("\n");
		}
		return sb.toString();
	}

	private static void appendPublisherNodeDefinition(final StringBuilder sb, final DtDefinition dtDefinition) {
		sb.append("PN_").append(dtDefinition.getLocalName()).append("  = new PublisherNode (\n");
		for (final DtField dtField : dtDefinition.getFields()) {
			final String fieldName = dtField.getName();
			if (DataType.Boolean == dtField.getDomain().getDataType()) {
				sb.append("\t\tbooleanField[").append(fieldName).append(")] = new DataField ();\n");
			} else if (DataType.DtObject == dtField.getDomain().getDataType()) {
				sb.append("\t\tdataField[").append(fieldName).append(")] = new NodeField (type = PN_").append(dtField.getDomain().getDtDefinition().getLocalName()).append(";);\n");
			} else if (DataType.DtList == dtField.getDomain().getDataType()) {
				sb.append("\t\tlistField[").append(fieldName).append(")] = new NodeField (type = PN_").append(dtField.getDomain().getDtDefinition().getLocalName()).append(";);\n");
			} else { //aussi si FieldType.FOREIGN_KEY == dtField.getType()
				sb.append("\t\tstringField[").append(fieldName).append(")] = new DataField ();\n");
			}
		}
		sb.append(");\n");

	}

	//	private void populateData(final DtObject dto, final PublisherDataNode publisherDataNode, final PublisherDataNodeDefinition publisherDataNodeDefinition) {
	//		Assertion.notNull(dto);
	//		Assertion.notNull(publisherDataNode);
	//		Assertion.notNull(publisherDataNodeDefinition);
	//		//---------------------------------------------------------------------
	//		final DtDefinition dtDefinition = dto.getDefinition();
	//		for (final PublisherField publisherField : publisherDataNodeDefinition.getFields()) {
	//			final String fieldName = publisherField.getName();
	//			final DtField dtField = dtDefinition.getField(fieldName);
	//			final Object value = dto.getValue(dtField);
	//			if (value != null) {
	//				switch (publisherField.getType()) {
	//					case Boolean:
	//						publisherDataNode.setBoolean(fieldName, (Boolean) value);
	//						break;
	//					case String:
	//						publisherDataNode.setStringValue(fieldName, DtHelper.getFormattedValue(dto, dtField));
	//						break;
	//					case List:
	//						final DtList<?> dtc = (DtList<?>) value;
	//						for (final DtObject element : dtc) {
	//							final PublisherDataNode elementPublisherDataNode = publisherDataNode.createNodeData(fieldName);
	//							populateData(element, elementPublisherDataNode, publisherDataNodeDefinition.getField(fieldName));
	//							publisherDataNode.addDataToList(fieldName, elementPublisherDataNode);
	//						}
	//						break;
	//					case Data:
	//						final DtObject element = (DtObject) value;
	//						final PublisherDataNode elementPublisherDataNode = publisherDataNode.createNodeData(fieldName);
	//						populateData(element, elementPublisherDataNode, publisherDataNodeDefinition.getField(fieldName));
	//						publisherDataNode.setData(fieldName, elementPublisherDataNode);
	//						break;
	//					default:
	//						throw new IllegalArgumentException("Type non g�r� : " + publisherField.getType());
	//				}
	//			}
	//		}
	//	}
}