//******************************************************************************
// Copyright (c) 2011 Synchrotron Soleil.
//
// This file is part of cdma-core library.
//
// The cdma-core library is free software; you can redistribute it and/or modify it 
// under the terms of the GNU General Public License as published by the Free 
// Software Foundation; either version 2 of the License, or (at your option) 
// any later version.
//
// The CDMA library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with cmda-python.  If not, see <http://www.gnu.org/licenses/>.
//
// Contributors :
// See AUTHORS file 
//******************************************************************************

#ifndef __CDMA_RANGE_H__
#define __CDMA_RANGE_H__

#include <cdma/Common.h>
#include <cdma/exception/Exception.h>

namespace cdma
{

// !! Range is a internal class !!
/// @cond internal

// Forward declaration
DECLARE_CLASS_SHARED_PTR(Range);

//==============================================================================
/// @brief A closed interval on an array dimension
///
/// A range represents a set of integers that are positions on one dimension. 
/// It is used as a View descriptor for Arrays on a particular dimension.
//==============================================================================
class Range
{
public:
  /// Default c-tor
  Range();

  /// c-tor with Range length
  ///
  /// @param length Range length
  ///
  Range( int length );

  /// c-tor with Range length
  ///
  /// @param name Range name
  /// @param first First position
  /// @param last Last position
  /// @param stride Distance between each consecutive positions
  /// @param reduced Is this range is visible trough the view
  ///
  Range( std::string name, long first, long last, long stride, bool reduced = false );
  Range( const cdma::Range& range );
  
  /// d-tor
  ~Range() { };

  /// Get the number of elements in the range.
  ///
  /// @return the number of elements in the range.
  ///
  int length() const ;
  
  /// Get the offset for this element: inverse of index.
  ///
  /// @param index the element of the range
  /// @return the offset corresponding to index-th element in the range.
  ///
  int element(int index) throw ( cdma::Exception );
  
  /// Returns the position of the given offset
  ///
  /// @param offset Element offset
  /// @return Index of the element
  /// @note the given offset is modified corresponding to what remains to the lower dimension
  ///
  int index(long& offset) throw ( cdma::Exception );
  
  /// Is the ith element contained in this Range?
  ///
  /// @param i index in the original Range
  /// @return true if the ith element would be returned by the Range iterator
  ///
  bool contains(int i) const;
  
  /// @return first element's offset in range
  ///
  int first() const;
  
  /// @return last element's offset in range, inclusive
  ///
  int last() const;
  
  /// @return stride, must be >= 1
  ///
  int stride() const;
  
  /// Get name.
  ///
  /// @return name, or empty string
  ///
  std::string getName() const;
  
  /// Find the smallest element k in the Range, such that
  /// <ul>
  /// <li>k >= first
  /// <li>k >= start
  /// <li>k <= last
  /// <li>k = first + i * stride for some integer i.
  /// </ul>
  ///
  /// @param start starting index
  /// @return first in interval, else -1 if there is no such element.
  ///
  int getFirstInInterval(int start);

  /// Returns true if this Range has been reduced
  /// (i.e not considered in View)
  ///
  bool reduce() const      { return m_reduced; };
  
  /// Set the reduced status (i.e true to not consider it in View)
  ///
  /// @param reduce boolean value
  ///
  void setReduce(bool reduce) { m_reduced = reduce; };

  /// Setters to redefine the Range
  void set( int length );
  void set( std::string name, long first, long last, long stride, bool reduced = false );
  void set( const cdma::Range& range );
  
 /// Create a new Range by composing a Range that is relative to this Range.
  ///
  /// @param r range relative to base
  /// @return combined Range, may be EMPTY
  ///
  RangePtr compose(const cdma::Range& r) throw ( cdma::Exception );
  
  /// Create a new Range by compacting this Range by removing the stride. first
  /// = first/stride, last=last/stride, stride=1.
  ///
  /// @return compacted Range
  ///
  RangePtr compact() throw ( cdma::Exception );
  
  /// Create a new Range shifting this range by a constant factor.
  ///
  /// @param origin subtract this from first, last
  /// @return shift range
  ///
  RangePtr shiftOrigin(int origin) throw ( cdma::Exception );
  
  /// Create a new Range by intersecting with a Range using same interval as
  /// this Range. NOTE: intersections when both Ranges have strides are not
  /// supported.
  ///
  /// @param r range to intersect
  /// @return intersected Range, may be EMPTY
  ///
  RangePtr intersect(const cdma::Range& r) throw ( cdma::Exception );
  
  /// Determine if a given Range intersects this one. NOTE: we dont yet support
  /// intersection when both Ranges have strides
  ///
  /// @param r range to intersect
  /// @return true if they intersect
  ///
  bool intersects(const cdma::Range& r);
  
  /// Create a new Range by making the union with a Range using same interval
  /// as this Range. NOTE: no strides.
  ///
  /// @param r range to add
  /// @return intersected Range, may be EMPTY
  ///
  RangePtr unionRanges(const cdma::Range& r) throw ( cdma::Exception );

private:
  int         m_last;     // offset of last element
  int         m_first;    // offset of first element
  int         m_stride;   // stride, must be >= 1
  int         m_length;   // number of element in that range
  bool        m_reduced;  // was this ranged reduced or not
  std::string m_name;     // optional name
};

/// @endcond internal

}

#endif
