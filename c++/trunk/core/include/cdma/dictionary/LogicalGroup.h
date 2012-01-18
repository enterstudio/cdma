/////***************************************************************************
// Copyright (c) 2011 Synchrotron Soleil.
// The CDMA library is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the Free
// Software Foundation; either version 2 of the License, or (at your option)
// any later version.
// Contributors :
// See AUTHORS file
/////***************************************************************************
#ifndef __CDMA_LOGICALGROUP_H__
#define __CDMA_LOGICALGROUP_H__

#include <list>
#include <string>
#include <map>

#include <yat/memory/SharedPtr.h>

#include <cdma/navigation/IContainer.h>
#include <cdma/IObject.h>

namespace cdma
{

//==============================================================================
/// The LogicalGroup is an IObject that is purely <b></b>. Its existence is
/// correlated to the Dictionary.
/// <p>
/// A standard CDMA dictionary make a link between a key and a path. Now let's imagine
/// a dictionary with keys having a tree structure. This structure hierarchically organized
/// might now have a meaning regardless their physical organization.
/// So the keys are now simple notions that can have a human friendly meaning.
/// <p>
/// The LogicalGroup permits to browse simply through those different levels
/// of key. More over the key used can be filtered according to some criteria.
/// The aim is to find a really specific node by doing a search that get narrower
/// while iterating over queries.
/// @todo remove inheritance from IObject
//==============================================================================
class LogicalGroup : public IObject
{
private:
  IDataset*                              m_pDataset;       ///< Simple pointer to the parent dataset
  LogicalGroup*                          m_pParent;        ///< Reference to the parent group (NULL if root)
  KeyPtr                                 m_keyPtr;         ///< Key from which this logical group was intantiated (is_null() = true if root)
  DictionaryPtr                          m_dictionaryPtr;  ///< Dictionary this logical group match to
  std::map<std::string, LogicalGroupPtr> m_childGroups;    ///< List of child groups
  StringListPtr                          m_listKeyPtr;

public:
  // Constructor
  LogicalGroup( IDataset* pDataset, LogicalGroup* pParent, const KeyPtr& pKey, const DictionaryPtr& dictionary );

  //Virtual destructor
  virtual ~LogicalGroup();

  /// Find the IDataItem corresponding to the given key in the dictionary.
  ///
  /// @param key
  /// 			entry of the dictionary (can carry filters)
  ///
  /// @return the first encountered DataItem that match the key, else null
  ///
  IDataItemPtr getDataItem(const KeyPtr& key);

  /// Find the DataItem corresponding to the given key in the dictionary.
  ///
  /// @param keyPath
  /// 			 separated entries of the dictionary (can't carry filters
  ///
  /// @return the first encountered DataItem that match the key, else null
  /// @note keyPath can contain several keys concatenated with a plug-in's separator
  ///
  IDataItemPtr getDataItem(const std::string& keyPath);

  /// Find all IDataItems corresponding to the given key in the dictionary.
  ///
  /// @param key
  /// 			entry of the dictionary (can carry filters)
  ///
  /// @return a std::list of DataItem that match the key
  ///
  std::list<IDataItemPtr> getDataItemList(const KeyPtr& key);

  /// Find all IDataItems corresponding to the given path of key in the dictionary.
  ///
  /// @param keyPath
  /// 			separated entries of the dictionary (can't carry filters)
  ///
  /// @return a std::list of DataItem that match the key
  /// @note keyPath can contain several keys concatenated with a plug-in's separator
  ///
  std::list<IDataItemPtr> getDataItemList(const std::string& keyPath);

  /// Find the Group corresponding to the given key in the dictionary.
  ///
  /// @param key
  ///            entry name of the dictionary
  /// @return the first encountered LogicalGroup that matches the key, else null
  ///
  LogicalGroupPtr getGroup(const KeyPtr& key);

  /// Find the Group corresponding to the given key in the dictionary.
  ///
  /// @param keyPath
  /// 			separated entries of the dictionary (can't carry filters)
  /// @return the first encountered LogicalGroup that matches the key, else null
  /// @note keyPath can contain several keys concatenated with a plug-in's separator
  ///
  LogicalGroupPtr getGroup(const std::string& keyPath);

  /// Return the std::list of key that match the given model type.
  ///
  /// @param model which kind of keys (ie: DataItem, Group, ILogical, Attribute...)
  ///
  /// @return List of type Group; may be empty, not null.
  ///
  std::list<std::string> getKeyNames(CDMAType::ModelType model);

  /// Return a list of available keys for this LogicalGroup
  ///
  /// @return List of keys that can be asked
  ///
  //StringListPtr getKeys();
  std::list<KeyPtr> getKeys();

  /// Bind the given key with the given name, so the key can be accessed by the bind
  ///
  /// @param bind
  /// 				value with which we can get the key
  /// @param key
  /// 				key object to be mapped by the bind value
  /// @return the given key
  ///
  KeyPtr bindKey(const std::string& bind, const KeyPtr& key);

  /// Return the std::list of parameters we can set on the given key that will
  /// have an occurrence in the dataset.
  /// @param key
  ///          Key for which we want the parameters values
  /// @return
  /// 			list<PathParameter> that can be directly applied on the key
  /// @note <b>EXPERIMENTAL METHOD</b> do note use/implements
  /// @note if the path that matches the key hold several different parameters
  /// the method will return the FIRST undefined parameter. To know deeper parameters,
  /// user has to set some PathParameter on the key and call again this method
  ///
  std::list<PathParameterPtr> getParameterValues(const KeyPtr& key);

  /// Get the parent of this logical group
  /// @return group LogicalGroup
  ///
  LogicalGroupPtr getParent();

  /// Set the given logical group as parent of this logical group
  /// @param group LogicalGroup
  ///
  void setParent(LogicalGroup& group);

  /// This method defines the way the IExtendedDictionary will be loaded.
  /// It must manage the do the detection and loading of the key file,
  /// and the corresponding mapping file that belongs to the plug-in.
  /// Once the dictionary has its paths targeting both key and mapping
  /// files set, the detection work is done. It just remains the loading
  /// of those files using the IExtendedDictionary.
  /// @return dictionary instance, that has already loaded keys and paths
  /// @note Dictionary.readEntries() is already implemented in the core
  ///
  //yat::SharedPtr<IExtendedDictionary, yat::Mutex> findAndReadDictionary();

  std::string getLocation();
  std::string getName();
  std::string getShortName();

  //@{IObject interface
  CDMAType::ModelType getModelType() const;
  std::string getFactoryName() const;
  //@} IObject interface
};
} //namespace cdma
#endif //__CDMA_LOGICALGROUP_H__