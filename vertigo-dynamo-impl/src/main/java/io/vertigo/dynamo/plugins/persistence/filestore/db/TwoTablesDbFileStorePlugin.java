package io.vertigo.dynamo.plugins.persistence.filestore.db;

import io.vertigo.dynamo.domain.metamodel.DataStream;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.metamodel.FileInfoDefinition;
import io.vertigo.dynamo.file.model.FileInfo;
import io.vertigo.dynamo.file.model.InputStreamBuilder;
import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.dynamo.impl.persistence.FileStorePlugin;
import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.lang.Assertion;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.inject.Inject;

/**
 * Permet de gérer le CRUD sur un fichier stocké sur deux tables (Méta données / Données).
 * 
 * @author sezratty
 * @version $Id: TwoTablesDbFileStorePlugin.java,v 1.8 2014/01/20 17:49:32 pchretien Exp $
 */
public final class TwoTablesDbFileStorePlugin implements FileStorePlugin {
	private static final String STORE_READ_ONLY = "Le store est en readOnly";

	/**
	 * Liste des champs du Dto de stockage.
	 * Ces champs sont obligatoire sur les Dt associés aux fileInfoDefinitions
	 */
	private static enum DtoFields {
		FILE_NAME, MIME_TYPE, LAST_MODIFIED, LENGTH, FILE_DATA, FMD_ID, FDT_ID
	}

	/**
	 * Le store est-il en mode readOnly ?
	 */
	private final boolean readOnly;
	private final FileManager fileManager;

	/**
	 * Constructeur.
	 * 
	 * @param fileManager Manager de gestion des fichiers
	 */
	@Inject
	public TwoTablesDbFileStorePlugin(final FileManager fileManager) {
		super();
		Assertion.checkNotNull(fileManager);
		// ---------------------------------------------------------------------
		readOnly = false;
		this.fileManager = fileManager;
	}

	/** {@inheritDoc} */
	public FileInfo load(final URI<FileInfo> uri) {
		// Ramène FileMetada
		final URI<DtObject> dtoMetaDataUri = createMetaDataURI(uri);
		final DtObject fileMetadataDto = getPersistenceManager().getBroker().get(dtoMetaDataUri);
		final Object fdtId = this.<Object> getValue(fileMetadataDto, DtoFields.FDT_ID);

		// Ramène FileData
		final URI<DtObject> dtoDataUri = createDataURI(uri.<FileInfoDefinition> getDefinition(), fdtId);
		final DtObject fileDataDto = getPersistenceManager().getBroker().get(dtoDataUri);
		// Construction du KFile.
		final InputStreamBuilder inputStreamBuilder = new DataStreamInputStreamBuilder(this.<DataStream> getValue(fileDataDto, DtoFields.FILE_DATA));
		final String fileName = this.<String> getValue(fileMetadataDto, DtoFields.FILE_NAME);
		final String mimeType = this.<String> getValue(fileMetadataDto, DtoFields.MIME_TYPE);
		final Date lastModified = this.<Date> getValue(fileMetadataDto, DtoFields.LAST_MODIFIED);
		final Long length = this.<Long> getValue(fileMetadataDto, DtoFields.LENGTH);
		final KFile kFile = fileManager.createFile(fileName, mimeType, lastModified, length, inputStreamBuilder);

		//TODO passer par une factory de FileInfo à partir de la FileInfoDefinition (comme DomainFactory)
		final FileInfo fileInfo = new DatabaseFileInfo(uri.<FileInfoDefinition> getDefinition(), kFile);
		fileInfo.setURIStored(uri);
		return fileInfo;
	}

	/** {@inheritDoc} */
	public void put(final FileInfo fileInfo) {
		Assertion.checkArgument(!readOnly, STORE_READ_ONLY);
		final boolean isCreation = fileInfo.getURI() == null;
		// ---------------------------------------------------------------------
		final DtObject fileMetadataDto = createMetadataDtObject(fileInfo.getDefinition());
		final DtObject fileDataDto = createDataDtObject(fileInfo.getDefinition());
		// ---------------------------------------------------------------------
		final KFile kFile = fileInfo.getKFile();
		setValue(fileMetadataDto, DtoFields.FILE_NAME, kFile.getFileName());
		setValue(fileMetadataDto, DtoFields.MIME_TYPE, kFile.getMimeType());
		setValue(fileMetadataDto, DtoFields.LAST_MODIFIED, kFile.getLastModified());
		setValue(fileMetadataDto, DtoFields.LENGTH, kFile.getLength());
		setValue(fileDataDto, DtoFields.FILE_NAME, kFile.getFileName());
		setValue(fileDataDto, DtoFields.FILE_DATA, new FileInfoDataStream(kFile));

		// ---------------------------------------------------------------------
		if (isCreation) {
			getPersistenceManager().getBroker().save(fileDataDto);
			setValue(fileMetadataDto, DtoFields.FDT_ID, DtObjectUtil.getId(fileDataDto));
			getPersistenceManager().getBroker().save(fileMetadataDto);

			final URI<FileInfo> uri = createURI(fileInfo.getDefinition(), DtObjectUtil.getId(fileMetadataDto));
			fileInfo.setURIStored(uri);
		} else {
			setValue(fileMetadataDto, DtoFields.FMD_ID, fileInfo.getURI().getKey());
			// Chargement du FDT_ID
			final URI<DtObject> dtoMetaDataUri = createMetaDataURI(fileInfo.getURI());
			final DtObject fileMetadataDtoOld = getPersistenceManager().getBroker().<DtObject> get(dtoMetaDataUri);
			final Object fdtId = this.<Object> getValue(fileMetadataDtoOld, DtoFields.FDT_ID);
			setValue(fileMetadataDto, DtoFields.FDT_ID, fdtId);
			setValue(fileDataDto, DtoFields.FDT_ID, fdtId);
			getPersistenceManager().getBroker().save(fileDataDto);
			getPersistenceManager().getBroker().save(fileMetadataDto);
		}
	}

	private static URI<FileInfo> createURI(final FileInfoDefinition fileInfoDefinition, final Object key) {
		return new URI<>(fileInfoDefinition, key);
	}

	/** {@inheritDoc} */
	public void remove(final URI<FileInfo> uri) {
		Assertion.checkArgument(!readOnly, STORE_READ_ONLY);
		// ---------------------------------------------------------------------
		final URI<DtObject> dtoMetaDataUri = createMetaDataURI(uri);
		final DtObject fileMetadataDtoOld = getPersistenceManager().getBroker().<DtObject> get(dtoMetaDataUri);
		final Object fdtId = this.<Object> getValue(fileMetadataDtoOld, DtoFields.FDT_ID);
		final URI<DtObject> dtoDataUri = createDataURI(uri.<FileInfoDefinition> getDefinition(), fdtId);

		getPersistenceManager().getBroker().delete(dtoDataUri);
		getPersistenceManager().getBroker().delete(dtoMetaDataUri);
	}

	/**
	 * Création d'une URI du DTO de métadonnées à partir de l'URI de FileInfo
	 * 
	 * @param uri URI de FileInfo
	 * @return URI du DTO utilisé en BDD pour stocker les métadonnées.
	 */
	private URI<DtObject> createMetaDataURI(final URI<FileInfo> uri) {
		Assertion.checkNotNull(uri, "uri du fichier doit être renseignée.");
		// ---------------------------------------------------------------------
		final FileInfoDefinition fileInfoDefinition = uri.<FileInfoDefinition> getDefinition();
		final DtDefinition dtDefinition = getRootDtDefinition(fileInfoDefinition, 0);
		return new URI<>(dtDefinition, uri.getKey());
	}

	/**
	 * Création d'une URI de DTO des données à partir de l'URI de FileInfo et la clé de la ligne
	 * 
	 * @param fileInfoDefinition Definition de FileInfo
	 * @param fdtId Identifiant de la ligne
	 * @return URI du DTO utilisé en BDD pour stocker les données.
	 */
	private URI<DtObject> createDataURI(final FileInfoDefinition fileInfoDefinition, final Object fdtId) {
		final DtDefinition dtDefinition = getRootDtDefinition(fileInfoDefinition, 1);
		return new URI<>(dtDefinition, fdtId);
	}

	private DtDefinition getRootDtDefinition(final FileInfoDefinition fileInfoDefinition, final int rootIndex) {
		Assertion.checkNotNull(fileInfoDefinition, "Definition du fichier doit être renseignée.");
		Assertion.checkNotNull(fileInfoDefinition.getRoot(), "Pour ce FileStore le root contient le nom des deux tables : FILE_METADATA;FILE_DATA");
		Assertion.checkArgument(fileInfoDefinition.getRoot().contains(";"), "Pour ce FileStore le root contient le nom des deux tables : FILE_METADATA;FILE_DATA");
		Assertion.checkArgument(fileInfoDefinition.getRoot().split(";").length == 2, "Pour ce FileStore le root contient le nom des deux tables : FILE_METADATA;FILE_DATA");
		// ---------------------------------------------------------------------
		final String fileDataDefinitionRoot = fileInfoDefinition.getRoot().split(";")[rootIndex];
		// Pour ce fileStore, on utilise le root des fileDefinitions comme nom des tables de stockage.
		// Il doit exister des DtObjet associés, avec la structure attendue.
		return Home.getDefinitionSpace().resolve(fileDataDefinitionRoot, DtDefinition.class);
	}

	private DtObject createMetadataDtObject(final FileInfoDefinition fileInfoDefinition) {
		return DtObjectUtil.createDtObject(getRootDtDefinition(fileInfoDefinition, 0));
	}

	private DtObject createDataDtObject(final FileInfoDefinition fileInfoDefinition) {
		return DtObjectUtil.createDtObject(getRootDtDefinition(fileInfoDefinition, 1));
	}

	/**
	 * Retourne une valeur d'un champ à partir du DtObject.
	 * 
	 * @param dto DtObject
	 * @param field Nom du champs
	 * @return Valeur typé du champ
	 */
	// non static pour le typage du generic O
	private <O> O getValue(final DtObject dto, final DtoFields field) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		final DtField dtField = dtDefinition.getField(field.name());
		return (O) dtField.getDataAccessor().getValue(dto);
	}

	/**
	 * Fixe une valeur d'un champ d'un DtObject.
	 * 
	 * @param dto DtObject
	 * @param field Nom du champs
	 * @param value Valeur
	 */
	private void setValue(final DtObject dto, final DtoFields field, final Object value) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		final DtField dtField = dtDefinition.getField(field.name());
		dtField.getDataAccessor().setValue(dto, value);
	}

	private static final class FileInfoDataStream implements DataStream {
		private final KFile kFile;

		FileInfoDataStream(final KFile kFile) {
			Assertion.checkNotNull(kFile);
			//-----------------------------------------------------------------
			this.kFile = kFile;
		}

		/** {@inheritDoc} */
		public InputStream createInputStream() throws IOException {
			return kFile.createInputStream();
		}

		/** {@inheritDoc} */
		public long getLength() {
			return kFile.getLength();
		}
	}

	private static final class DataStreamInputStreamBuilder implements InputStreamBuilder {
		private final DataStream dataStream;

		DataStreamInputStreamBuilder(final DataStream dataStream) {
			Assertion.checkNotNull(dataStream);
			//-----------------------------------------------------------------
			this.dataStream = dataStream;
		}

		/** {@inheritDoc} */
		public InputStream createInputStream() throws IOException {
			return dataStream.createInputStream();
		}
	}

	private static PersistenceManager getPersistenceManager() {
		return Home.getComponentSpace().resolve(PersistenceManager.class);
	}
}