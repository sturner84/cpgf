#ifndef CPGF_GMETACOMMON_H
#define CPGF_GMETACOMMON_H

#include "cpgf/gcompiler.h"
#include "cpgf/gconfig.h"
#include "cpgf/gvariant.h"
#include "cpgf/gscopedptr.h"
#include "cpgf/gclassutil.h"
#include "cpgf/gmetaextendtype.h"
#include "cpgf/gapi.h"

#include "cpgf/private/gmetacommon_p.h"

#include <stdexcept>
#include <vector>

#include <ctype.h>
#include <string.h>


#define REF_MAX_ARITY G_MAX_ARITY

namespace cpgf {

class GMetaType;
class GTypeInfo;
class GMetaClass;
class GMetaEnum;
class GMetaFundamental;
class GMetaAnnotation;
class GMetaConverter;
struct IMemoryAllocator;

const size_t abstractParameterIndexBase = 0x1000000;

struct GMetaVariadicParam
{
	GVariant const * const * params;
	size_t paramCount;
};

enum GMetaVisibility {
	mPrivate = 1,
	mProtected = 2,
	mPublic = 4,
};


enum GMetaCategory {
	mcatField = 0,
	mcatProperty = 1,
	mcatMethod = 2,
	mcatEnum = 3,
	mcatOperator = 4,
	mcatConstructor = 5,
	mcatClass = 6,
	mcatAnnotation = 7,
	mcatFundamental = 8,
	mcatNonReflected = 9,
	mcatBaseClass = 10, /* Note: this is for non-reflected items only */
	mcatCount = 11
};

const int metaModifierNone = 0;
const int metaModifierStatic = 1 << 0;
const int metaModifierNoFree = 1 << 1;
const int metaModifierVirtual = 1 << 2;
const int metaModifierPureVirtual = 1 << 3;
const int metaModifierTemplate = 1 << 4;
const int metaModifierConst = 1 << 5;
const int metaModifierVolatile = 1 << 6;
const int metaModifierInline = 1 << 7;
const int metaModifierExplicit = 1 << 8;
const int metaModifierExtern = 1 << 9;
const int metaModifierMutable = 1 << 10;



class GMetaItem : public GNoncopyable
{
public:
	GMetaItem(const char * name, const GMetaType & itemType,
			GMetaCategory category, const char * nameSpace);
	virtual ~GMetaItem();

	bool isStatic() const {
		return this->hasModifier(metaModifierStatic);
	}

	GMetaCategory getCategory() const {
		return this->category;
	}

	void addModifier(int m) {
		this->modifiers |= m;
	}

	bool hasModifier(int m) const {
		return (this->modifiers & m) != 0;
	}

	int getModifiers() const {
		return this->modifiers;
	}

	const GMetaItem * getOwnerItem() const {
		return this->ownerItem;
	}

	virtual const GMetaType & getItemType() const;
	virtual GMetaExtendType getItemExtendType(uint32_t flags) const = 0;

	const std::string & getName() const;
	const std::string & getQualifiedName() const;
	const std::string & getNamespace() const;
	std::string makeQualifiedName(const char * delimiter) const;
	
	bool equals(const GMetaItem * other) const;

	const GMetaAnnotation * getAnnotation(const char * name) const;
	size_t getAnnotationCount() const;
	const GMetaAnnotation * getAnnotationAt(size_t index) const;

protected:
	void setName(const char * name);

private:
	GMetaAnnotation * addItemAnnotation(GMetaAnnotation * annotation);

protected:
	GScopedPointer<meta_internal::GMetaItemImplement> implement;

private:
	int modifiers;
	GMetaCategory category;
	GMetaItem * ownerItem;

private:
	friend class GMetaClass;

	template <typename ClassType, typename DerivedType>
	friend class GDefineMetaCommon;
};


class GMetaTypedItem : public GMetaItem
{
private:
	typedef GMetaItem super;

public:
	GMetaTypedItem(const char * name, const GMetaType & itemType, GMetaCategory category, const char * nameSpace);
	virtual ~GMetaTypedItem();

	const GMetaType & getMetaType() const;
	virtual size_t getTypeSize() const = 0;

	virtual void * createInstance() const = 0;
	virtual void * createInplace(void * placement) const = 0;
	virtual void * cloneInstance(const void * instance) const = 0;
	virtual void * cloneInplace(const void * instance, void * placement) const = 0;

	virtual void destroyInstance(void * instance) const = 0;
	virtual void destroyInplace(void * instance) const = 0;
};


class GMetaAccessible : public GMetaItem
{
private:
	typedef GMetaItem super;

public:
	GMetaAccessible(const char * name, const GMetaType & itemType, GMetaCategory category, const char * nameSpace);

	virtual bool canGet() const = 0;
	virtual bool canSet() const = 0;

	virtual GVariant get(const void * instance) const = 0;
	virtual void set(void * instance, const GVariant & v) const = 0;
	
	virtual void * getAddress(const void * instance) const = 0;

	virtual size_t getSize() const = 0;

};


class GMetaCallable : public GMetaItem
{
private:
	typedef GMetaItem super;

public:
	GMetaCallable(const char * name, const GMetaType & itemType, GMetaCategory category, const char * nameSpace);

	virtual size_t getParamCount() const = 0;
	virtual GMetaType getParamType(size_t index) const = 0;
	virtual size_t getDefaultParamCount() const = 0;
	virtual bool hasResult() const = 0;
	virtual GMetaType getResultType() const = 0;
	virtual GMetaExtendType getResultExtendType(uint32_t flags) const = 0;
	virtual GMetaExtendType getParamExtendType(uint32_t flags, size_t index) const = 0;
	virtual bool isVariadic() const = 0;
	virtual bool isExplicitThis() const = 0;
	virtual bool checkParam(const GVariant & param, size_t paramIndex) const = 0;
	virtual bool isParamTransferOwnership(size_t paramIndex) const = 0;
	virtual bool isResultTransferOwnership() const = 0;
	virtual GVariant execute(void * obj, const GVariant * params, size_t paramCount) const = 0;

};


GMAKE_FINAL(GMetaList)

class GMetaList : GFINAL_BASE(GMetaList)
{
public:
	GMetaList();
	~GMetaList();

	GMetaList(const GMetaList & other);
	GMetaList & operator = (const GMetaList & other);

	void add(const GMetaItem * item, void * instance);
	size_t getCount() const;
	const GMetaItem * getAt(size_t index) const;
	void * getInstanceAt(size_t index) const;
	void clear();

private:
	std::vector<const GMetaItem *> itemList;
	std::vector<void *> instanceList;
};


bool metaIsField(int category);
bool metaIsProperty(int category);
bool metaIsMethod(int category);
bool metaIsEnum(int category);
bool metaIsOperator(int category);
bool metaIsConstructor(int category);
bool metaIsClass(int category);;
bool metaIsAnnotation(int category);
bool metaIsFundamental(int category);
/**
 * Determines if this is the non-reflected category
 * @param category Category to check
 * @return true if the category is mcatNonReflected
 */
bool metaIsNonReflected(int category);


} // namespace cpgf




#endif
