#ifndef __GMETAARCHIVEWRITER_H
#define __GMETAARCHIVEWRITER_H

#include "gmetaarchivecommon.h"
#include "cpgf/ginterface.h"
#include "cpgf/gflags.h"
#include "cpgf/gmetaapi.h"


namespace cpgf {



class GMetaArchiveWriterConfig : public GMetaArchiveConfig
{
};

struct IMetaWriter : public IObject
{
	virtual void G_API_CC writeFundamental(const char * name, uint32_t archiveID, const GVariantData * value) = 0;
	virtual void G_API_CC writeString(const char * name, uint32_t archiveID, const char * value) = 0;
	virtual void G_API_CC writeNullPointer(const char * name) = 0;

	virtual void G_API_CC beginWriteObject(const GMetaArchiveObjectInformation * objectInformation) = 0;
	virtual void G_API_CC endWriteObject(const GMetaArchiveObjectInformation * objectInformation) = 0;

	virtual void G_API_CC writeReferenceID(const char * name, uint32_t archiveID, uint32_t referenceArchiveID) = 0;
	virtual void G_API_CC writeClassType(const char * name, uint32_t archiveID, IMetaClass * metaClass) = 0;

//	virtual void G_API_CC beginWriteArray(const char * name, uint32_t length, IMetaTypedItem * typeItem) = 0;
//	virtual void G_API_CC endWriteArray(const char * name, uint32_t length, IMetaTypedItem * typeItem) = 0;
};

struct IMetaArchiveWriter {};

enum GMetaArchivePointerType {
	aptByValue, aptByPointer, aptIgnore
};

class GMetaArchiveWriterPointerTracker;
class GMetaArchiveWriterClassTypeTracker;

class GMetaArchiveWriter : public IMetaArchiveWriter
{
public:
	GMetaArchiveWriter(const GMetaArchiveWriterConfig & config, IMetaService * service, IMetaWriter * writer);
	~GMetaArchiveWriter();

	// take care of customized serializer, take care of pointer resolve.
	void writeObjectValue(const char * name, void * instance, IMetaClass * metaClass);
	void writeObjectPointer(const char * name, void * instance, IMetaClass * metaClass);
	void writeField(const char * name, void * instance, IMetaAccessible * accessible);
	
	// ignore customized serializer, take care of pointer resolve.
	void defaultWriteObjectValue(const char * name, void * instance, IMetaClass * metaClass);
	void defaultWriteObjectPointer(const char * name, void * instance, IMetaClass * metaClass);
	void defaultWriteField(const char * name, void * instance, IMetaAccessible * accessible);

	// ignore customized serializer, ignore pointer resolve, take care of base classes
	void directWriteObject(const char * name, void * instance, IMetaClass * metaClass);
	void directWriteField(const char * name, void * instance, IMetaAccessible * accessible);

	// ignore customized serializer, ignore pointer resolve, ignore base classes, only write the object itself
	void directWriteObjectWithoutBase(const char * name, void * instance, IMetaClass * metaClass);

	void beginWriteObject(const char * name, uint32_t archiveID, void * instance, IMetaClass * metaClass, uint32_t classTypeID);
	void endWriteObject(const char * name, uint32_t archiveID, void * instance, IMetaClass * metaClass, uint32_t classTypeID);
	
protected:
	void writeObjectHelper(const char * name, void * instance, IMetaClass * metaClass, GMetaArchivePointerType pointerType);
	void defaultWriteObjectHelper(const char * name, void * instance, IMetaClass * metaClass, GMetaArchivePointerType pointerType);
	
	void doWriteObject(uint32_t archiveID, void * instance, IMetaClass * metaClass, GMetaArchivePointerType pointerType);
	void doDefaultWriteObject(uint32_t archiveID, void * instance, IMetaClass * metaClass, GMetaArchivePointerType pointerType);
	void doDirectWriteObject(uint32_t archiveID, void * instance, IMetaClass * metaClass);
	void doDirectWriteObjectWithoutBase(uint32_t archiveID, void * instance, IMetaClass * metaClass);
	
	void doWriteField(const char * name, void * instance, IMetaAccessible * accessible);
	void doDefaultWriteField(const char * name, void * instance, IMetaAccessible * accessible);
	void doDirectWriteField(const char * name, void * instance, IMetaAccessible * accessible);
	
	uint32_t getClassTypeID(void * instance, IMetaClass * metaClass, GMetaArchivePointerType pointerType);

	uint32_t getNextArchiveID();

	GMetaArchiveWriterPointerTracker * getPointerTracker();
	GMetaArchiveWriterClassTypeTracker * getClassTypeTracker();

private:
	GMetaArchiveWriterConfig config;
	GScopedInterface<IMetaService> service;
	GScopedInterface<IMetaWriter> writer;
	uint32_t currentArchiveID;
	GScopedPointer<GMetaArchiveWriterPointerTracker> pointerSolver;
	GScopedPointer<GMetaArchiveWriterClassTypeTracker> classTypeTracker;
};



} // namespace cpgf


#endif