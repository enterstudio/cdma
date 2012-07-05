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
 * Created on: Jun 29, 2011
 *     Author: Eugen Wintersberger <eugen.wintersberger@desy.de>
 */

#ifndef __SELECTION_HPP__
#define __SELECTION_HPP__

#include <iostream>
#include <cdma/exception/Exception.h>
#include <boost/python.hpp>
#include <boost/python/slice.hpp>

#include "Types.hpp"
#include "Exceptions.hpp"

using namespace cdma;
using namespace boost::python;

class Selection
{
    private:
        std::vector<size_t> _offset; //!< offset of the selection
        std::vector<size_t> _stride; //!< steps between elements
        std::vector<size_t> _shape;  //!< number of elements along each dimension
    public:
        //===================constructors and destructor=======================
        //! default constructor
        Selection():_offset(0),_stride(0),_shape(0) {}

        //----------------------------------------------------------------------
        //! copy constructor
        Selection(const Selection &s):
            _offset(s._offset),
            _stride(s._stride),
            _shape(s._shape)
        {}

        //----------------------------------------------------------------------
        //! move constructor
        Selection(Selection &&s):
            _offset(std::move(s._offset)),
            _stride(std::move(s._stride)),
            _shape(std::move(s._shape))
        {}

        //----------------------------------------------------------------------
        //! standard constructor
        Selection(const std::vector<size_t> &o,
                  const std::vector<size_t> &st,std::vector<size_t> sh):
            _offset(o),
            _stride(st),
            _shape(sh)
        {}

        //---------------------------------------------------------------------
        //! destructor
        ~Selection(){}

        //===================assignment operators==============================
        //! copy assignment operator
        Selection &operator=(const Selection &s)
        {
            if(this == &s) return *this;
            _offset = s._offset;
            _stride = s._stride;
            _shape  = s._shape;
            return *this;
        }

        //---------------------------------------------------------------------
        //! move assignemnt operator
        Selection &operator=(Selection &&s)
        {
            if(this == &s) return *this;

            _offset = std::move(s._offset);
            _stride = std::move(s._stride);
            _shape  = std::move(s._shape);
            return *this;
        }

        //! get offset of the selection 
        const std::vector<size_t> &offset() const { return _offset; }

        //! get the stride of the selection
        const std::vector<size_t> &stride() const { return _stride; }

        //! return the shape of the selection
        const std::vector<size_t> &shape() const  { return _shape;  }

        //! return the rank of the selection
        size_t rank() const { return _shape.size(); }

};

//! output operator
std::ostream &operator<<(std::ostream &o,const Selection &s);

//-----------------------------------------------------------------------------
/*! 
\brief compute the size of a selection

The size of the selection is the number of elements included in the selection. 
\param s selection object
\return number of elements in the selection
*/
size_t size(const Selection &sel);

//-----------------------------------------------------------------------------
/*! 
\brief compute the span of a selection

The span of a selection is the number of elements the selection covers in the
original data.
\param s selection object
\return number of elements spanned in the original array
*/
size_t span(const Selection &sel);
    
//-----------------------------------------------------------------------------
void set_selection_parameters_from_index(size_t i,const extract<size_t> &index,
                                         std::vector<size_t> &offset,
                                         std::vector<size_t> &stride,
                                         std::vector<size_t> &shape);

template<typename WTYPE>
void set_selection_parameters_from_slice(size_t i,const extract<slice> &slice,
                                         const WTYPE &o,
                                         std::vector<size_t> &offset,
                                         std::vector<size_t> &stride,
                                         std::vector<size_t> &shape)
{
    //now we have to investigate the components of the 
    //slice
    boost::python::ssize_t start = 0;
    extract<size_t> __start(slice().start());
    if(__start.check()) start = __start();
   
    boost::python::ssize_t step = 1;
    extract<size_t> __step(slice().step());
    if(__step.check()) step = __step();

    boost::python::ssize_t stop = o.shape()[i];
    extract<boost::python::ssize_t> __stop(slice().stop());
    if(__stop.check()) stop = __stop();

    //configure the selection
    offset[i] = start;
    stride[i] = step;
    
    boost::python::ssize_t res = (stop-start)%step;
    shape[i] = (stop-start-res)/step;
}


/*! 
\brief create a selection from a python object

Create a selection object from the python argument passed to __getitem__ or
__setitem__. This argument can be either a single value or a tuple in the case
of a multidimensional array.
\param t python tuple with indices, slices and ellipsis
\return a selection object for the wrapper classes
*/
template<typename WTYPE>
Selection create_selection(const WTYPE &o,const tuple &t)
{
    //setup selection parameters
    std::vector<size_t> offset(o.rank());
    std::vector<size_t> stride(o.rank());
    std::vector<size_t> shape(o.rank());

    //the number of elements in the tuple must not be equal to the 
    //rank of the field. This is due to the fact that the tuple can contain
    //one ellipsis which spans over several dimensions.

    bool has_ellipsis = false;
    size_t ellipsis_size = 0;
    if(len(t) > (ssize_t)o.rank())
        throw_cdma_exception<ShapeNotMatchException>(
                "Tuple with indices, slices, and ellipsis is longer than the "
                "rank of the field - something went wrong here",
                "template<typename WTYPE> Selection create_selection(const "
                "WTYPE &o,const tuple &pyselection)");
    else if(len(t) != (ssize_t)o.rank())
    {
        //here we have to fix the size of an ellipsis
        ellipsis_size = o.rank()-(len(t)-1);
    }

    /*this loop has tow possibilities:
    -> there is no ellipse and the rank of the field is larger than the size of
       the tuple passed. In this case an IndexError will occur. In this case we 
       know immediately that a shape error occured.
    */
    for(size_t i=0,j=0;i<o.rank();i++,j++)
    {
        //manage a single index
        extract<size_t> index(t[j]);
        if(index.check())
        {
            set_selection_parameters_from_index(i,index,offset,stride,shape);
            continue;
        }

        //manage a slice
        extract<slice> s(t[j]);
        if(s.check())
        {
            set_selection_parameters_from_slice(i,s,o,offset,stride,shape);
            continue;
        }

        //if we came until here the only possible type the tuples component can
        //be an ellipsis. If it is not an ellipsis an exception will be thrown.
        const object &ellipsis = t[j];
        if(Py_Ellipsis != ellipsis.ptr())
            throw_PyTypeError("A selection must contain only indices,slices"
                              "and a single ellipsis!");

        //assume here that the object is an ellipsis - this is a bit difficult
        //to handle as we do not know over how many 
        if(!has_ellipsis)
        {
            has_ellipsis = true;
            while(i<j+ellipsis_size)
            {
                stride[i] = 1;
                offset[i] = 0;
                shape[i] = o.shape()[i];
                i++;
            }
        }else{
            //throw index error exception here since there is only one ellipsis
            //allowed in the tuple
        }
    }

    //once we are done with looping over all elemnts in the tuple we need 
    //to adjust the selection to take into account an ellipsis
    if((ellipsis_size) && (!has_ellipsis))
        throw_cdma_exception<ShapeNotMatchException>(
                "Selection rank does not match DataItem rank",
                "template<typename WTYPE> Selection create_selection(const "
                "WTYPE &o,const tuple &pyselection)");

    return Selection(offset,stride,shape);
}

#endif