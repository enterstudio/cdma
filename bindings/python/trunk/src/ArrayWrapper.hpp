/*
 * (c) Copyright 2012 DESY, Eugen Wintersberger <eugen.wintersberger@desy.de>
 *
 * This file is part of cdma-python.
 *
 * cdma-python is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * cdma-python is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with cmda-python.  If not, see <http://www.gnu.org/licenses/>.
 *************************************************************************
 *
 * Created on: Jul 03, 2011
 *     Author: Eugen Wintersberger <eugen.wintersberger@desy.de>
 */

#ifndef __ARRAYWRAPPER_HPP__
#define __ARRAYWRAPPER_HPP__

#include <algorithm>
#include<cdma/array/IArray.h>
#include "Types.hpp"

using namespace cdma;

/*! 
\ingroup wrapper_classes
\brief ArrayWrapper class

This class wraps around an ArrayPtr type. This class is not intended to be
exposed to Python but is within the process of harmonizing the interface between
 data holdgin types (like attributes and datasets)
*/
class ArrayWrapper
{
    private:
        IArrayPtr _ptr; //!< pointer to the wrapped array object
    public:
        //================constructors and destructor==========================
        //! default constructor
        ArrayWrapper():_ptr(NULL) {}

        //---------------------------------------------------------------------
        //! standard constructor
        ArrayWrapper(IArrayPtr ptr):_ptr(ptr) {}

        //---------------------------------------------------------------------
        //! copy constructor
        ArrayWrapper(const ArrayWrapper &a):_ptr(a._ptr) {}

        //---------------------------------------------------------------------
        //! destructor
        ~ArrayWrapper() {}

        //========================assignment operators=========================
        //! copy assignment operator
        ArrayWrapper &operator=(const ArrayWrapper &a)
        {
            if(this == &a) return *this;
            _ptr = a._ptr;
            return *this;
        }

        //=====================================================================
        /*! 
        \brief get rank

        Return the number of dimensios of the array.
        \return number of dimension
        */
        size_t rank() const { return size_t(_ptr->getRank()); }

        //---------------------------------------------------------------------
        /*! 
        \brief get number of elements

        returns the number of elements stored in the array.
        \return array size
        */
        size_t size() const { return size_t(_ptr->getSize()); }

        //----------------------------------------------------------------------
        /*! 
        \brief get shape

        Return a vector with the number of elements along each dimension. 
        \return shape vector
        */
        std::vector<size_t> shape() const 
        {   
            std::vector<size_t> s(this->rank());
            std::vector<int> os = _ptr->getShape();

            std::copy(os.begin(),os.end(),s.begin());
            return s;
        }

        //----------------------------------------------------------------------
        /*! 
        \brief get type ID

        Return the ID of the data type used to store the data.
        \return type ID
        */
        TypeID type() const 
        { 
            return TypeUtility::typename2typeid(_ptr->getValueType().name()); 
        }

        //----------------------------------------------------------------------
        /*! 
        \brief read scalar data

        Read scalar data if the array stores only a single value. 
        \return scalar data value
        */
        template<typename T> T get() const
        {
            return _ptr->getValue<T>();
        }

        //---------------------------------------------------------------------
        /*! 
        \brief read from position
        
        Read data from a particular array index pos. 
        \param pos array index from which to read data
        \return data at index
        */
        template<typename T> T get(const std::vector<size_t> &pos) const
        {
            return _ptr->getValue<T>(pos);
        }

        //---------------------------------------------------------------------
        /*! 
        \brief read part of the array

        */
        ArrayWrapper get(const std::vector<size_t> offset,
                         const std::vector<size_t> shape) const
        {
            std::vector<int> _offset;
            std::vector<int> _shape;

            std::copy(offset.begin(),offset.end(),_offset.begin());
            std::copy(shape.begin(),shape.end(),_shape.begin());

            ArrayWrapper a(_ptr->getRegion(_offset,_shape));
            return a;
        }

        //---------------------------------------------------------------------
        //! get pointer to data
        const void *ptr() const
        {
            return _ptr->getStorage()->getStorage();
        }
};

#endif
