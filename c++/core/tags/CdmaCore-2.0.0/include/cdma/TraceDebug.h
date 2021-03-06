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

#ifndef __CDMA_TRACE_DEBUG_H__
#define __CDMA_TRACE_DEBUG_H__

#include <string>
#include <iostream>
#include <iomanip>
#include <yat/threading/Utilities.h>
#include <yat/threading/Mutex.h>

/// @cond internal

namespace cdma
{

#ifdef CDMA_DEBUG
  
  #define CDMA_DBG_PREFIX(x) "[" << \
                          std::hex << std::setfill(' ') << \
                          std::setw(10) << \
                          (const void*)(x) << \
                          "][" << \
                          std::setfill('0') << \
                          std::setw(8) << \
                          yat::ThreadingUtilities::self() << \
                          "] " << \
                          std::dec

  // !!! DO NOT INSTANTIATE THIS CLASS !!!
  class dbg_helper
  {
    private:
      std::string _s;
      const void *_this_object;

    public:
      dbg_helper(const std::string &s, const void* this_object=(const void*)(0x12345678)) : _s(s), _this_object(this_object)
      {
        //yat::AutoMutex<> _lock(mutex());
        std::cout << CDMA_DBG_PREFIX(_this_object) << indent() << "> " << _s << std::endl;
        indent().append(2, ' ');
      }
      ~dbg_helper()
      {
       // yat::AutoMutex<> _lock(mutex());
        indent().erase(0,2);
        std::cout << CDMA_DBG_PREFIX(_this_object) << indent() << "< " << _s << std::endl;
      }
      static std::string &indent()
      {
        static std::string indent;
        return indent;
      }
      static yat::Mutex &mutex()
      {
        static yat::Mutex mtx;
        return mtx;
      }
  };

  /// A function trace helper
  #define CDMA_FUNCTION_TRACE(function_name) cdma::dbg_helper _func_trace(function_name, this)
  #define CDMA_STATIC_FUNCTION_TRACE(function_name) cdma::dbg_helper _func_trace(function_name)

  /// A simple trace helper
  #define CDMA_TRACE(s)  std::cout << CDMA_DBG_PREFIX(this) << dbg_helper::indent() << s << std::endl
  #define CDMA_STATIC_TRACE(s)  std::cout << CDMA_DBG_PREFIX(0x12345678) << dbg_helper::indent() << s << std::endl
  #define CDMA_COND_TRACE(c, s) do { if(c) { CDMA_TRACE(s); } } while(0)
  #define CDMA_STATIC_COND_TRACE(c, s) do { if(c) { CDMA_STATIC_TRACE(s); } } while(0)

#else
  class dbg_helper;
  #define CDMA_FUNCTION_TRACE(s)
  #define CDMA_TRACE(s)
  #define CDMA_STATIC_FUNCTION_TRACE(s)
  #define CDMA_STATIC_TRACE(s)
  #define CDMA_COND_TRACE(c, s)
  #define CDMA_STATIC_COND_TRACE(c, s)
#endif

} // namespace

/// @endcond internal

#endif // __CDMA_TRACE_DEBUG_H__
