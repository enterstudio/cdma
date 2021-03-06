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
#include <cdma/Common.h>
#include <cdma/exception/impl/ExceptionImpl.h>
#include <cdma/array/impl/View.h>

#include <stdlib.h>

namespace cdma
{

//---------------------------------------------------------------------------
// View::View
//---------------------------------------------------------------------------
View::View()
{
  m_rank = 0;
  m_upToDate = false;
  m_lastIndex = 0;
}

//---------------------------------------------------------------------------
// View::View
//---------------------------------------------------------------------------
View::View(const cdma::IViewPtr& view)
{
//  CDMA_FUNCTION_TRACE("View::View");
  m_rank = view->getRank();
  
  // Create new ranges
#ifdef CDMA_STD_SMART_PTR
  std::vector<Range> ranges = (std::dynamic_pointer_cast<View>(view))->m_ranges;
#else
  std::vector<Range> ranges = ViewPtr(view)->m_ranges;
#endif

  m_ranges.resize( ranges.size() );
  for( yat::uint16 i = 0; i < ranges.size(); i++ )
  {
    m_ranges[i].set(
      ranges[i].getName(),
      ranges[i].first(),
      ranges[i].last(),
      ranges[i].stride(),
      ranges[i].reduce()
    );
  }
  
  m_upToDate = false;
}

//---------------------------------------------------------------------------
// View::View
//---------------------------------------------------------------------------
View::View(int rank, int shape[], int start[])
{
//  CDMA_FUNCTION_TRACE("View::View");
  m_rank = rank;
  m_ranges.resize(m_rank);

  // Create new ranges
  long stride = 1;
  for( int i = m_rank - 1; i >= 0; i-- )
  {
    m_ranges[i].set(
      "",
      start[i] * stride,
      (start[i] + shape[i] - 1) * stride,
      stride
    );
    stride *= shape[i];
  }
  m_upToDate = false;
}

//---------------------------------------------------------------------------
// View::View
//---------------------------------------------------------------------------
View::View(std::vector<int> shape, std::vector<int> start)
{
//  CDMA_FUNCTION_TRACE("View::View");
  m_rank = shape.size();
  m_ranges.resize(m_rank);

  // Create new ranges
  long stride = 1;
  for( int i = m_rank - 1; i >= 0; i-- )
  {
    m_ranges[i].set(
      "",
      start[i] * stride,
      (start[i] + shape[i] - 1) * stride,
      stride
    );
    stride *= shape[i];
  }
  m_upToDate = false;
}

//---------------------------------------------------------------------------
// View::~View
//---------------------------------------------------------------------------
View::View(std::vector<int> shape, std::vector<int> start, std::vector<int> stride)
{
//  CDMA_FUNCTION_TRACE("View::View");
  m_rank = shape.size();
  m_ranges.resize(m_rank);

  int delta = 0;
  
  // Create new ranges
  for( int i = m_rank - 1; i >= 0; i-- )
  {
    // Array starts at index 0 so the length when first = 0, last = 1023 and stride=1 is 1024
    // if first = 1023, last = 0 and stride = -1 is 1024
    // Delta is used to convert length into offset index in an array
    if( stride[i] >= 0 )
    {
      delta = -1;
    }
    else
    {
      delta = 1;
    }

    m_ranges[i].set(
      "",
      start[i] * abs(stride[i]),
      (start[i] * abs(stride[i]) ) + ( shape[i] + delta) * stride[i],
      stride[i]
    );
  }
  m_upToDate = false;

}

//---------------------------------------------------------------------------
// View::~View
//---------------------------------------------------------------------------
View::~View()
{
  CDMA_FUNCTION_TRACE("View::~View");
}

//---------------------------------------------------------------------------
// View::getRank
//---------------------------------------------------------------------------
int View::getRank()
{
  return m_rank;
}

//---------------------------------------------------------------------------
// View::getShape
//---------------------------------------------------------------------------
std::vector<int> View::getShape()
{
  //CDMA_FUNCTION_TRACE("View::getShape");
  std::vector<int> shape;
  for( yat::uint16 i = 0; i < m_ranges.size(); i++ )
  {
    // Only consider not reduced ranges
    if( ! m_ranges[i].reduce() )
    {
      shape.push_back( m_ranges[i].length() );
    }
  }
  return shape;
}

//---------------------------------------------------------------------------
// View::getOrigin
//---------------------------------------------------------------------------
std::vector<int> View::getOrigin()
{
  std::vector<int> origin;
  for( yat::uint16 i = 0; i < m_ranges.size(); i++ )
  {
    // Only consider not reduced ranges
    if( ! m_ranges[i].reduce() )
    {
      origin.push_back( (int) abs(m_ranges[i].first() / m_ranges[i].stride()) );
    }
  }
  return origin;
}

//---------------------------------------------------------------------------
// View::getSize
//---------------------------------------------------------------------------
long View::getSize()
{
  long size = 1;
  if( m_rank == 0 )
  {
    size = 0;
  }
  else
  {
    for(yat::uint16 i = 0; i < m_ranges.size(); i++ )
    {
      // Only consider not reduced ranges
      if( ! m_ranges[i].reduce() )
      {
        size *= m_ranges[i].length();
      }
    }
  }
  return size;
}

//---------------------------------------------------------------------------
// View::getStride
//---------------------------------------------------------------------------
std::vector<int> View::getStride()
{
  std::vector<int> stride;
  for( yat::uint16 i = 0; i < m_ranges.size(); i++ )
  {
    // Only consider not reduced ranges
    if( ! m_ranges[i].reduce() )
    {
      stride.push_back( m_ranges[i].stride() );
    }
  }
  return stride;
}

//---------------------------------------------------------------------------
// View::getElementOffset
//---------------------------------------------------------------------------
long View::getElementOffset(std::vector<int> position)
{
  long value = 0;

  try 
  {
    int j = 0;
    // For each range additionate offset matching the position
    for( yat::uint16 i = 0; i < m_ranges.size(); i++ )
    {
      // If range not reduced calculate offset of the corresponding position
      if( ! m_ranges[i].reduce() && position.size() > 0 )
      {
        value += (m_ranges[i]).element( position[j] );
        j++;
      }
      // If range is reduced calculate its first element's offset
      else
      {
        value += (m_ranges[i]).element( 0 );
      }
    }
    
    // If view is a composition projects position into that view
    if( m_compound )
    {
      std::vector<int> projection = getPositionElement( value );
      value = m_compound->getElementOffset( projection );
    }
  } 
  catch( cdma::Exception& )
  {
    // An error occured returns -1 that is an invalid result
    value = -1;
  }

  return value;
}

//---------------------------------------------------------------------------
// View::getPositionElement
//---------------------------------------------------------------------------
std::vector<int> View::getPositionElement(long offset)
{
  std::vector<int> position;
  try 
  {
    for( yat::uint16 i = 0; i < m_ranges.size(); i++ )
    {
      if( ! m_ranges[i].reduce() )
      {
        position.push_back( m_ranges[i].index(offset) );
      }
    }
  } catch( cdma::Exception& )
  {
    position = std::vector<int>();
  }

  return position;
}

//---------------------------------------------------------------------------
// View::compose
//---------------------------------------------------------------------------
void View::compose(const IViewPtr& higher_view)
{
#ifdef CDMA_STD_SMART_PTR
  m_compound = std::dynamic_pointer_cast<View>(higher_view);
#else
  m_compound = higher_view;
#endif
}

//---------------------------------------------------------------------------
// View::lastElement
//---------------------------------------------------------------------------
long View::lastElement()
{
  if( ! m_upToDate )
  {
    long last = 0;
    for( yat::uint16 i = 0; i < m_ranges.size(); i++ )
    {
      last += m_ranges[i].last();
    }
    m_lastIndex = last;
    m_upToDate = true;
  }
  return m_lastIndex;
}

//---------------------------------------------------------------------------
// View::set
//---------------------------------------------------------------------------
/*
void View::set(std::vector<int> position)
{
  if( position.size() != m_rank )
  {
    VIEW_ERROR("Given position doesn't match to view's rank!", "View::set");
  }

  // Only consider not reduced ranges
  int j = 0;
  for( int i = 0; i < m_ranges.size(); i++ )
  {
    if( ! m_ranges[i].reduce() )
    {
      m_vCurPos[i] = position[j];
      j++;
    }
  }
}
*/
//---------------------------------------------------------------------------
// View::setDim
//---------------------------------------------------------------------------
/*
void View::setDim(int dim, int value)
{
  if( dim >= m_rank || dim < 0 )
  {
    VIEW_ERROR("Requested dimension doesn't exist!", "View::setDim");
  }
  
  // Do not count reduced ranges (in dimension calculation)
  int d = dim;
  int j = 0;
  for( int i = 0; j <= dim; i++)
  {
    if( m_ranges[i].reduce() )
    {
      d++;
    }
    else
    {
      j++;
    }
  }
  
  if ( value >= 0 && value < m_ranges[d].length() )
  {
    m_vCurPos[d] = value;
  }
  else
  {
    VIEW_ERROR("Requested position is out of range!", "View::setDim");
  }
}
*/
//---------------------------------------------------------------------------
// View::setOrigin
//---------------------------------------------------------------------------
void View::setOrigin(std::vector<int> origin)
{
  CDMA_FUNCTION_TRACE("View::setOrigin");
  if( origin.size() != (unsigned int)m_rank )
  {
    THROW_EXCEPTION("VIEW_ERROR", "Origin must have same length as view's rank!", "View::setOrigin");
  }
  yat::uint16 i = 0;
  yat::uint16 j = 0;
  while( i < origin.size() )
  {
    // Only consider not reduced ranges
    if( ! m_ranges[j].reduce() )
    {
      m_ranges[j].set(
        m_ranges[j].getName(),
        (long) origin[i] * abs(m_ranges[j].stride()),
        (long) origin[i] * abs(m_ranges[j].stride()) + (m_ranges[j].length() - 1) * m_ranges[j].stride() ,
        (long) m_ranges[i].stride(),
        m_ranges[j].reduce()
      );
      i++;
    }
    j++;
  }
  
  m_upToDate = false;
}

//---------------------------------------------------------------------------
// View::setShape
//---------------------------------------------------------------------------
void View::setShape(std::vector<int> shape)
{
  if( shape.size() != (unsigned int) m_rank )
  {
    THROW_EXCEPTION("VIEW_ERROR", "Origin must have same length as view's rank!", "View::setShape");
  }

  m_upToDate = false;
  yat::uint16 i = 0;
  yat::uint16 j = 0;
  while( i < shape.size() )
  {
    // Only consider not reduced ranges
    if( ! m_ranges[j].reduce() )
    {
      m_ranges[j].set(
        m_ranges[j].getName(),
        (long) m_ranges[j].first(),
        (long) m_ranges[j].first() + m_ranges[j].stride() * (shape[i] - 1),
        (long) m_ranges[j].stride(),
        m_ranges[j].reduce()
      );
      i++;
    }
    j++;
  }
}

//---------------------------------------------------------------------------
// View::setStride
//---------------------------------------------------------------------------
void View::setStride(std::vector<int> stride)
{
  if( stride.size() != (unsigned int)m_rank )
  {
    THROW_EXCEPTION("VIEW_ERROR", "Origin must have same length as view's rank!", "View::setStride");
  }

  m_upToDate = false;
  yat::uint16 i = 0;
  yat::uint16 j = 0;
  while( i < stride.size() )
  {
    // Only consider not reduced ranges
    if( ! m_ranges[j].reduce() )
    {
      m_ranges[j].set(
      m_ranges[j].getName(),
      (long) m_ranges[j].first(),
      (long) m_ranges[j].last(),
      (long) stride[i],
      m_ranges[j].reduce()
      );
      i++;
    }
    j++;
  }
}

//---------------------------------------------------------------------------
// View::setViewName
//---------------------------------------------------------------------------
void View::setViewName(int dim, const std::string& viewName)
{
  if( dim >= m_rank || dim < 0 )
  {
    THROW_EXCEPTION("VIEW_ERROR", "Requested range is out of rank!", "View::setViewName");
  }
  Range range ( viewName, m_ranges[dim].first(), m_ranges[dim].last(), m_ranges[dim].stride() );
  m_ranges[dim] = range;
}

//---------------------------------------------------------------------------
// View::getViewName
//---------------------------------------------------------------------------
std::string View::getViewName(int dim)
{
  if( dim >= m_rank || dim < 0 )
  {
    THROW_EXCEPTION("VIEW_ERROR", "Requested range is out of rank!", "View::getViewName");
  }
  return m_ranges[dim].getName();
}

//---------------------------------------------------------------------------
// View::reduce
//---------------------------------------------------------------------------
void View::reduce()
{
  CDMA_FUNCTION_TRACE("View::reduce");
  for( yat::uint16 i = 0; i < m_ranges.size(); i++ )
  {
    if( m_ranges[i].length() == 1 && ! m_ranges[i].reduce() )
    {
      m_ranges[i].setReduce(true);
      m_rank--;
    }
  }
  
  m_upToDate = false;
}

//---------------------------------------------------------------------------
// View::reduce
//---------------------------------------------------------------------------
void View::reduce(int dim) throw ( cdma::Exception )
{
    int i = 0;
    int range = -1;
    for(yat::uint16 j = 0; j < m_ranges.size(); j++)
    {
      if( ! m_ranges[j].reduce() )
      {
        if( i == dim ) 
        {
          range = i;
        }
        i++;
      }
    }

    if( (dim < 0) || (dim >= m_rank) ) 
    {
        THROW_EXCEPTION("VIEW_ERROR", "Illegal reduce dim " + ('0' + dim), "View::reduce" );
    }
    if( m_ranges[range].length() != 1 ) 
    {
        THROW_EXCEPTION("VIEW_ERROR", std::string("Illegal reduce dim " + ('0' + dim)) + " : reduced dimension must be have length=1", "View::reduce");
    }

    // Reduce proper range
    m_ranges[range].setReduce(true);
    m_rank--;
    m_upToDate = false;
}

}
