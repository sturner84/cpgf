#ifndef CPGF_GCONFIG_H
#define CPGF_GCONFIG_H


#define G_MAX_ARITY 30

//define this macro if you want to customize it
//#define G_API_CC __stdcall

//scturner method fix
//define this macro if you want to support more detailed type information
//#define G_ADD_TYPE_INFO

//#ifdef G_ADD_TYPE_INFO
//defines an extra parameter for methods
//#define G_ADD_TYPE_INFO_PARAM_LAST , const char * paramTypes = NULL, const char * nameSpace = NULL
//#define G_ADD_TYPE_INFO_PARAM_ONLY const char * paramTypes = NULL, const char * nameSpace = NULL
//#define G_ADD_TYPE_INFO_PARAM_PARAMS_LAST , const char * paramTypes = NULL
//defines an extra parameter to be sent to methods
//#define G_ADD_TYPE_INFO_ACT_PARAM_LAST , paramTypes, nameSpace
//#define G_ADD_TYPE_INFO_ACT_PARAM_ONLY paramTypes, nameSpace
//#define G_ADD_TYPE_INFO_ACT_PARAMS_LAST , paramTypes
//#define G_ADD_TYPE_INFO_ACT_NAMESPACE_LAST , nameSpace
//#else
//#define G_ADD_TYPE_INFO_PARAM_LAST
//#define G_ADD_TYPE_INFO_PARAM_ONLY
//#define G_ADD_TYPE_INFO_PARAM_PARAMS_LAST
//defines an extra parameter to be sent to methods
//#define G_ADD_TYPE_INFO_ACT_PARAM_LAST
//#define G_ADD_TYPE_INFO_ACT_PARAM_ONLY
//#define G_ADD_TYPE_INFO_ACT_PARAMS_LAST
//#define G_ADD_TYPE_INFO_ACT_NAMESPACE_LAST
//#endif

//TODO converts the type info of arrays to pointers
//#define G_CONVERT_ARRAYS_TO_POINTERS

#define G_TRACE


#endif

