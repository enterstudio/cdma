package org.gumtree.data.soleil.dictionary;

import org.gumtree.data.Factory;
import org.gumtree.data.IFactory;
import org.gumtree.data.dictionary.IExtendedDictionary;
import org.gumtree.data.dictionary.ILogicalGroup;
import org.gumtree.data.dictionary.impl.ExtendedDictionary;
import org.gumtree.data.dictionary.impl.LogicalGroup;
import org.gumtree.data.exception.FileAccessException;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IKey;
import org.gumtree.data.soleil.NxsFactory;
import org.gumtree.data.soleil.internal.DictionaryDetector;
import org.gumtree.data.soleil.navigation.NxsDataset;

public class NxsLogicalGroup extends LogicalGroup {
	public NxsLogicalGroup(IDataset dataset, IKey key) {
		this(dataset, key, false);
	}
	
	public NxsLogicalGroup(IDataset dataset, IKey key, boolean debug) {
    	super(key, dataset, debug);
    }
    
	public NxsLogicalGroup(ILogicalGroup parent, IKey key, IDataset dataset) {
		this( parent, key, dataset, false);
	}

    public NxsLogicalGroup(ILogicalGroup parent, IKey key, IDataset dataset, boolean debug) {
    	super(parent, key, dataset, debug);
    }
	
	public IExtendedDictionary findAndReadDictionary() {
		IFactory factory = NxsFactory.getInstance();
		IExtendedDictionary dictionary;

		// Detect the key dictionary file and mapping dictionary file
		try {
			String keyFile = Factory.getKeyDictionaryPath();
			String mapFile = Factory.getMappingDictionaryFolder( factory ) + detectDictionaryFile();

			dictionary = new ExtendedDictionary( NxsFactory.getInstance(), keyFile, mapFile );
        	dictionary.readEntries();
        } catch (FileAccessException e) {
            e.printStackTrace();
            dictionary = null;
		}
        
    	return dictionary;
    }
	
	/**
	 * According to the current corresponding dataset, this method will try
	 * to guess which XML dictionary mapping file should be used
	 * @return
	 * @throws FileAccessException 
	 */
	protected String detectDictionaryFile() throws FileAccessException {
		String mapFile;
		DictionaryDetector detector = new DictionaryDetector( (NxsDataset) super.getDataset() );
		mapFile = detector.getDictionaryName();
		return mapFile;
	}
	

}
