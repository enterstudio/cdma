// ****************************************************************************
// Synchrotron SOLEIL
//
// Creation : 08/12/2011
// Author   : See AUTHORS file
//
// This program is free software; you can redistribute it and/or modify it under
// the terms of the GNU General Public License as published by the Free Software
// Foundation; version 2 of the License.
//
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
// FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
//
// ****************************************************************************

#include <yat/plugin/PlugInSymbols.h>

#include <SoleilNxsFactory.h>
#include <NxsDataset.h>
#include <cdma/dictionary/Key.h>

EXPORT_SINGLECLASS_PLUGIN(cdma::SoleilNxsFactory, \
                          cdma::SoleilNxsFactoryInfo);

namespace cdma
{

const std::string PlugInID         ( "SoleilNeXus" );
const std::string InterfaceName    ( "cdma::IFactory" );
const std::string VersionNumber    ( "1.0.0" );

//----------------------------------------------------------------------------
// SoleilNxsFactoryInfo::get_plugin_id
//----------------------------------------------------------------------------
std::string SoleilNxsFactoryInfo::get_plugin_id() const
{
  return PlugInID;
}

//----------------------------------------------------------------------------
// SoleilNxsFactoryInfo::get_interface_name
//----------------------------------------------------------------------------
std::string SoleilNxsFactoryInfo::get_interface_name() const
{
  return InterfaceName;
}

//----------------------------------------------------------------------------
// SoleilNxsFactoryInfo::get_version_number
//----------------------------------------------------------------------------
std::string SoleilNxsFactoryInfo::get_version_number() const
{
  return VersionNumber;
}

//==============================================================================
// class SoleilNxsFactory
//==============================================================================
//----------------------------------------------------------------------------
// SoleilNxsFactory::openDataset
//----------------------------------------------------------------------------
IDatasetPtr SoleilNxsFactory::openDataset(const std::string& uri) throw ( cdma::Exception )
{
  THROW_NOT_IMPLEMENTED("SoleilNxsFactory::openDataset");
}

/*
//----------------------------------------------------------------------------
// SoleilNxsFactory::openDataset
//----------------------------------------------------------------------------
IDatasetPtr SoleilNxsFactory::openDataset(const yat::URI& uri) throw ( cdma::Exception )
{
  THROW_NOT_IMPLEMENTED("SoleilNxsFactory::openDataset");
}
*/

//----------------------------------------------------------------------------
// SoleilNxsFactory::openDictionary
//----------------------------------------------------------------------------
DictionaryPtr SoleilNxsFactory::openDictionary(const std::string& filepath) throw ( cdma::Exception )
{
  THROW_NOT_IMPLEMENTED("SoleilNxsFactory::openDictionary");
}

//----------------------------------------------------------------------------
// SoleilNxsFactory::createArray
//----------------------------------------------------------------------------
ArrayPtr SoleilNxsFactory::createArray(const std::type_info clazz, const std::vector<int> shape)
{
  THROW_NOT_IMPLEMENTED("SoleilNxsFactory::createArray");
}

//----------------------------------------------------------------------------
// SoleilNxsFactory::createArray
//----------------------------------------------------------------------------
ArrayPtr SoleilNxsFactory::createArray(const std::type_info clazz, const std::vector<int> shape, const void * storage)
{
  THROW_NOT_IMPLEMENTED("SoleilNxsFactory::createArray");
}

//----------------------------------------------------------------------------
// SoleilNxsFactory::createArray
//----------------------------------------------------------------------------
ArrayPtr SoleilNxsFactory::createArray(const void * array)
{
  THROW_NOT_IMPLEMENTED("SoleilNxsFactory::createArray");
}

//----------------------------------------------------------------------------
// SoleilNxsFactory::createStringArray
//----------------------------------------------------------------------------
ArrayPtr SoleilNxsFactory::createStringArray(const std::string& value)
{
  THROW_NOT_IMPLEMENTED("SoleilNxsFactory::createStringArray");
}

//----------------------------------------------------------------------------
// SoleilNxsFactory::createDoubleArray
//----------------------------------------------------------------------------
ArrayPtr SoleilNxsFactory::createDoubleArray(double array[])
{
  THROW_NOT_IMPLEMENTED("SoleilNxsFactory::createDoubleArray");
}

//----------------------------------------------------------------------------
// SoleilNxsFactory::createDoubleArray
//----------------------------------------------------------------------------
ArrayPtr SoleilNxsFactory::createDoubleArray(double array[], const std::vector<int> shape)
{
  THROW_NOT_IMPLEMENTED("SoleilNxsFactory::createDoubleArray");
}

//----------------------------------------------------------------------------
// SoleilNxsFactory::createArrayNoCopy
//----------------------------------------------------------------------------
ArrayPtr SoleilNxsFactory::createArrayNoCopy(const void * array)
{
  THROW_NOT_IMPLEMENTED("SoleilNxsFactory::createArrayNoCopy");
}

//----------------------------------------------------------------------------
// SoleilNxsFactory::createDataItem
//----------------------------------------------------------------------------
IDataItemPtr SoleilNxsFactory::createDataItem(const cdma::IGroupPtr& parent, const std::string& shortName, const cdma::ArrayPtr& array) throw ( cdma::Exception )
{
  THROW_NOT_IMPLEMENTED("SoleilNxsFactory::createDataItem");
}

//----------------------------------------------------------------------------
// SoleilNxsFactory::createGroup
//----------------------------------------------------------------------------
IGroupPtr SoleilNxsFactory::createGroup(const cdma::IGroupPtr& parent, const std::string& shortName, const bool updateParent)
{
  THROW_NOT_IMPLEMENTED("SoleilNxsFactory::createGroup");
}

//----------------------------------------------------------------------------
// SoleilNxsFactory::createGroup
//----------------------------------------------------------------------------
IGroupPtr SoleilNxsFactory::createGroup(const std::string& shortName) throw ( cdma::Exception )
{
  THROW_NOT_IMPLEMENTED("SoleilNxsFactory::createGroup");
}

//----------------------------------------------------------------------------
// SoleilNxsFactory::createAttribute
//----------------------------------------------------------------------------
IAttributePtr SoleilNxsFactory::createAttribute(const std::string& name, const void * value)
{
  THROW_NOT_IMPLEMENTED("SoleilNxsFactory::createAttribute");
}

//----------------------------------------------------------------------------
// SoleilNxsFactory::createDatasetInstance
//----------------------------------------------------------------------------
IDatasetPtr SoleilNxsFactory::createDatasetInstance(const std::string& uri) throw ( cdma::Exception )
{
  CDMA_FUNCTION_TRACE("SoleilNxsFactory::createDatasetInstance");
  return new NxsDataset(uri /*, this*/);
}

//----------------------------------------------------------------------------
// SoleilNxsFactory::createEmptyDatasetInstance
//----------------------------------------------------------------------------
IDatasetPtr SoleilNxsFactory::createEmptyDatasetInstance() throw ( cdma::Exception )
{
  CDMA_FUNCTION_TRACE("SoleilNxsFactory::createEmptyDatasetInstance");
  THROW_NOT_IMPLEMENTED("SoleilNxsFactory::createEmptyDatasetInstance");
}

//----------------------------------------------------------------------------
// SoleilNxsFactory::createKey
//----------------------------------------------------------------------------
KeyPtr SoleilNxsFactory::createKey(std::string keyName)
{
  return new cdma::Key(keyName);
}

//----------------------------------------------------------------------------
// SoleilNxsFactory::createPath
//----------------------------------------------------------------------------
PathPtr SoleilNxsFactory::createPath( std::string path )
{
  THROW_NOT_IMPLEMENTED("SoleilNxsFactory::createPath");
}

//----------------------------------------------------------------------------
// SoleilNxsFactory::createPathParameter
//----------------------------------------------------------------------------
PathParameterPtr SoleilNxsFactory::createPathParameter(cdma::CDMAType::ParameterType type, const std::string& name, void * value)
{
  THROW_NOT_IMPLEMENTED("SoleilNxsFactory::createPathParameter");
}

//----------------------------------------------------------------------------
// SoleilNxsFactory::createPathParamResolver
//----------------------------------------------------------------------------
IPathParamResolverPtr SoleilNxsFactory::createPathParamResolver(const cdma::PathPtr& path)
{
  THROW_NOT_IMPLEMENTED("SoleilNxsFactory::createPathParamResolver");
}

//----------------------------------------------------------------------------
// SoleilNxsFactory::getPathSeparator
//----------------------------------------------------------------------------
std::string SoleilNxsFactory::getPathSeparator()
{
  THROW_NOT_IMPLEMENTED("SoleilNxsFactory::getPathSeparator");
}

} // namespace cdma