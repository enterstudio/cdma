#setup script for libpninx-python package
import sys
import os
from distutils.core import setup
from distutils.extension import Extension
from distutils.sysconfig import get_python_inc
from distutils.fancy_getopt import FancyGetopt
from distutils.fancy_getopt import fancy_getopt
from distutils.ccompiler import new_compiler
from distutils.unixccompiler import UnixCCompiler

import commands

def pkgconfig(*packages, **kw):
    flag_map = {'-I': 'include_dirs', '-L': 'library_dirs', '-l': 'libraries'}
    for token in commands.getoutput("pkg-config --libs --cflags %s" % ' '.join(packages)).split():
        kw.setdefault(flag_map.get(token[:2]), []).append(token[2:])

    kw["libraries"].append("boost_python")
    return kw

#setup(
#    name = "myPackage",
#    ext_modules=[
#        Extension("extension", ["extension_main.c"], ),
#    ],
#)

#add here some options to handle additional compiler parameters
cliopts =[]
cliopts.append(("h5libdir=",None,"HDF5 library path"))
cliopts.append(("h5incdir=",None,"HDF5 include path"))
cliopts.append(("h5libname=",None,"HDF5 library name"))
cliopts.append(("nxlibdir=",None,"PNI NX library path"))
cliopts.append(("nxincdir=",None,"PNI NX include path"))
cliopts.append(("utlibdir=",None,"PNI utilities library path"))
cliopts.append(("utincdir=",None,"PNI utilities include path"))
cliopts.append(("numpyincdir=",None,"Numpy include path"))
cliopts.append(("noforeach",None,"Set noforeach option for C++"))

op = FancyGetopt(option_table=cliopts)
args,opts = op.getopt()

include_dirs = ["/usr/include/pni/hdf5"]
library_dirs = []

try: include_dirs.append(opts.h5incdir)
except: pass

try: include_dirs.append(opts.nxincdir)
except: pass

try: include_dirs.append(opts.utincdir)
except: pass

try: library_dirs.append(opts.h5libdir)
except: pass

try: library_dirs.append(opts.nxlibdir)
except: pass

try: library_dirs.append(opts.utlibdir)
except: pass

try: include_dirs.append(opts.numpyincdir)
except:pass

#in the end we need to add the Python include directory
include_dirs.append(get_python_inc())

cc = new_compiler()
try:
    cc.set_executables(compiler_so = os.environ['CC'])
except:
    print "Environment variable CC not found!"

compile_args = ["-std=c++0x","-g","-O0"]
#now we try to compile the test code
try:
    print "run compiler test for nullptr ..."
    cc.compile(['ccheck/nullptr_check.cpp'],extra_preargs=compile_args)
    print "compiler supports nullptr - passed!"
except:
    print "no nullptr support!"
    compile_args.append("-Dnullptr=NULL")

try:
    print "run compiler check for foreach loops ..."
    cc.compile(['ccheck/foreach_check.cpp'],extra_preargs=compile_args)
    print "compiler supports foreach loops!"
except:
    print "no support for foreach loops!"
    compile_args.append("-DNOFOREACH")

libs = ["boost_python","pniutils","pninx","pnihdf5"]


files = ["src/cdma.cpp","src/Factory.cpp","src/GroupWrapper.cpp",
         "src/DatasetWrapper.cpp","src/DataItemWrapper.cpp",
         "src/Exceptions.cpp"]

cdma = Extension("cdmacore",files,
                 extra_compile_args = compile_args,
                 **pkgconfig('cdmacore'))
#                 libraries=libs,
#                 library_dirs=library_dirs,
#                 include_dirs=include_dirs)

setup(name="cmda-python",
        author="Eugen Wintersberger",
        author_email="eugen.wintersberger@desy.de",
        description="Python bindings for CDMA ",
        version = "0.1.0",
        ext_package="cdma",
        ext_modules=[cdma],
        packages = ["cdma"],
        #url="https://sourceforge.net/projects/libpninxpython/",
        #script_args = args
        )
