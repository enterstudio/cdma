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

//-----------------------------------------------------------------------------
// DEPENDENCIES
//-----------------------------------------------------------------------------
#include <cdma/array/Slicer.h>
#include <cdma/array/SliceIterator.h>

namespace cdma
{
//---------------------------------------------------------------------------
// Slicer::Slicer
//---------------------------------------------------------------------------
Slicer::Slicer(const IArrayPtr& array, int dim)
{
  CDMA_FUNCTION_TRACE("Slicer::Slicer");
  m_array = array;
  m_rank = dim;
}

//---------------------------------------------------------------------------
// Slicer::~Slicer
//---------------------------------------------------------------------------
Slicer::~Slicer()
{
  CDMA_FUNCTION_TRACE("Slicer::~Slicer");
}

//---------------------------------------------------------------------------
// Slicer::next
//---------------------------------------------------------------------------
SliceIterator Slicer::begin()
{
  return SliceIterator( m_array, m_rank );
}

//---------------------------------------------------------------------------
// Slicer::end
//---------------------------------------------------------------------------
SliceIterator Slicer::end()
{
  return SliceIterator( m_array, m_rank );
}

//---------------------------------------------------------------------------
// Slicer::operator*
//---------------------------------------------------------------------------
const IArrayPtr& Slicer::array()
{
  return m_array;
}

//---------------------------------------------------------------------------
// Slicer::getSliceShape
//---------------------------------------------------------------------------
std::vector<int> Slicer::getSliceShape()
{
  std::vector<int> result;
  std::vector<int> shape = m_array->getShape();
  
  for( yat::uint16 i = shape.size() - m_rank - 1; i < shape.size(); i++ )
  {
    result.push_back( shape[i] );
  }
  
  return result;
}

}
