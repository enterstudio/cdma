# Introduction #

Since almost 20 years, the scientific community of neutron and synchrotron facilities has been dreaming of using a common data format to be able to exchange experimental results and to applications to analyse them. If using HDF5 as a physical container for data became quickly raised a large consensus, the big issue is the standardisation of data organisation. By introducing a new level of indirection for data access, the CommonDataModelAccess (CDMA) framework proposes a solution and allows splitting development efforts and responsibilities between institutes.

The CDMA is made of a core API that access data through a data format plug-ins mechanism and scientific applications definitions (sets of keywords) coming from a consensus between scientists and institutes. Using a innovative “mapping” system between applications definitions and physical data organizations the CDMA allows developing data reduction application regardless data files formats AND organizations. Each institute have to develop data access plug-ins for its own data files formats along with the mapping between application definitions and its data files. Thus data reduction applications can be developed by from a strictly scientific point of view and immediately having the capability to process data coming from several institutes.


# Genesis of the project #

Working independently, ESRF, SOLEIL, DESY and ANSTO software development has recently focussed on the design of frameworks for data processing operating on top of a NeXus-like (NeXus data format: http://www.nexusformat.org) data storage layer.

The central issue for a collaboration was related to use of the same tools independent of the file storage data format. Work at ANSTO on the GumTree Data Model (http://gumtree.codehaus.org) abstracted data file access by designing a data model with a set of Java interfaces. This seemed to be a very promising development to share.

SOLEIL became soon interested in the concept as it was coincidentally looking for a unified data access layer based on NeXus (the standard data format used at SOLEIL) to build on top of it its COMETE (http://comete.sourceforge.net) project, a Java framework that aims to ease data visualisation and data analysis applications programming.

The collaboration started between ANSTO and SOLEIL in January, 2010. The work started from the data access layer of the ANSTO's GumTree project (http://gumtree.codehaus.org/GumTree+Data+Model), written in Java.
At 2011/Q4, DESY will join the collaboration and help us developing the C++ port of CDMA.


# Our motivations #

The important thing we must understand is **the data format is not an issue**.

The issue is: how allowing users of our institutes to use the same tools regardless the origin of the data? We can notice that even in the same synchrotron facility, data aren't stored the same ways across the beamlines.

The aim of the CDMA is to offer a abstract data access layer in order to build analysis/reduction applications regardless the data formats. HDF-like formats allow the recording of any kind of data using a API. It abstracted the physical file organization. NeXus-like specification is a set of logical data organizations in an standardization effort. This kind of standardisation may be applicable of various tree-oriented data format (like HDF or XML). The problem of this approach is that facility must produce data file strictly conform with the specification. It's an huge challenge because each facility staff (mostly scientists & engineers)  have its own view on acquisition system and the produced experimental and contextual data. Also the hardware and the acquisition process are rarely driven by the data recording system.

The idea behind the CDMA is to reverse the data point of view. Rather than desperately try to standardize the data files across institutes, is it finally easier to introduce a layer able to hide the different way the data is stored? We think the answer is yes!

Thus, each institute will continue to produce data using the most suitable format. No longer need to wait for the ultimate data  organization specification before running acquisitions!