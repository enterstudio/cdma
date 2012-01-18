//*****************************************************************************
// Synchrotron SOLEIL
//
// Creation : 08/12/2011
// Author   : Rodriguez Clément
//
// This program is free software; you can redistribute it and/or modify it under
// the terms of the GNU General Public License as published by the Free Software
// Foundation; version 2 of the License.
//
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
// FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
//
//*****************************************************************************
#ifndef __CDMA_IDATA_H__
#define __CDMA_IDATA_H__

#include <yat/any/Any.h>
#include <cdma/IObject.h>
#include <vector>

//==============================================================================
/// IArrayStorage interface its the physical container of the memory matrix.
/// Its aim is to do the calculation of matrix's cells index according to a
/// a View and a position vector. Mainly to give a read/write access into
/// memory buffer.
//==============================================================================

namespace cdma
{

class IArrayStorage : public IObject
{
public:
  /// Get the "value" from the memory buffer according the position in the given view.
  ///
  /// @param view to consider for the index calculation
  /// @param position into which the value will be set
  /// @return yat::Any value
  ///
  virtual yat::Any& get( const cdma::ViewPtr& view, std::vector<int> position ) = 0;

  /// Set "value" in the memory buffer according the position in the given view. The 
  /// given yat::Any will be casted into memory buffer type.
  ///
  /// @param view to consider for the index calculation
  /// @param position into which the value will be set
  /// @param value to be set
  ///
  virtual void set(const cdma::ViewPtr& view, std::vector<int> position, const yat::Any& value) = 0;

  /// Returns the type_info of the underlying canonical data
  ///
  virtual const std::type_info& getType() = 0;

  /// Returns true if the memory has been modified since last read
  ///
  virtual bool dirty() = 0;
  
  /// Set the dirty flag to given boolean.
  ///
  virtual void setDirty(bool dirty) = 0;
  
  /// Returns the underlying buffer as it is in memory
  ///
  /// @note use this method at your own risk
  ///
  virtual void* getStorage() = 0;
  
  /// Create a copy of this IArrayStorage, copying the data so that physical order is
  /// the same as logical order.
  ///
  /// @return the new IArrayStorage with copied memory storage
  /// @note be aware: can lead to out of memory 
  ///
  virtual IArrayStoragePtr deepCopy() = 0;
};

}

#endif