package com.github.hakko.musiccabinet.dao;

import java.util.Set;

import com.github.hakko.musiccabinet.domain.model.library.File;
import com.github.hakko.musiccabinet.domain.model.library.MetaData;

public interface LibraryAdditionDao {

	void clearImport();

	void addSubdirectories(String directory, Set<String> subDirectories);

	void addFiles(String directory, Set<File> files);

	void updateLibrary();

	void updateMetadata(String directory, String filename, MetaData md);

}