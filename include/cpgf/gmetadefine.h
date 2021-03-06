#ifndef CPGF_GMETADEFINE_H
#define CPGF_GMETADEFINE_H

#include "cpgf/gmetaannotation.h"
#include "cpgf/gmetaclass.h"
#include "cpgf/gmetaenum.h"
#include "cpgf/gmetafield.h"
#include "cpgf/gmetamethod.h"
#include "cpgf/gmetaoperator.h"
#include "cpgf/gmetaproperty.h"
#include "cpgf/gglobal.h"
#include "cpgf/gpp.h"
#include "cpgf/gsharedptr.h"


#define MAX_BASE_COUNT 20
#define BASE_DEFAULT(N, unused) , typename GPP_CONCAT(BaseType, N) = void


namespace cpgf {


namespace meta_internal {


#define CASE_SUPERLIST_ARG(N, ARRAY) case N: \
	superList->add<ClassType, typename TypeList_GetWithDefault<TList, N, void>::Result>( ARRAY [ N ] ); \
	break;

template <typename TList, typename ClassType>
GMetaSuperList * doMakeSuperList(int modifiers[]) {
	GMetaSuperList * superList = new GMetaSuperList;

	for(int i = 0; i < MAX_BASE_COUNT; ++i) {
		if(i >= static_cast<int>(TypeList_Length<TList>::Result)) {
			break;
		}

		switch(i) {
		GPP_REPEAT(19, CASE_SUPERLIST_ARG, modifiers)

		default:
			break;
		}
	}

	return superList;
}

#undef CASE_SUPERLIST_ARG

template <typename DefineClass>
struct GLazyDefineClassHelper
{
	static void (*registerAddress)(DefineClass define);

	static void metaRegister(GMetaClass * metaClass)
	{
		DefineClass define(metaClass, metaClass);
		registerAddress(define);
	}
};

template <typename DefineClass>
void (*GLazyDefineClassHelper<DefineClass>::registerAddress)(DefineClass define) = NULL;

typedef GSharedPointer<GMetaClass> GSharedMetaClass;


} // namespace meta_internal


template <typename BaseType>
class GDefineMetaConstructor : public BaseType
{
private:
	typedef GDefineMetaConstructor<BaseType> ThisType;

public:
	GDefineMetaConstructor(meta_internal::GSharedMetaClass metaClass, GMetaConstructor * constructor) : BaseType(metaClass, constructor) {
	}

	ThisType _default(const GVariant & value) {
		gdynamic_cast<GMetaConstructor *>(this->currentItem)->addDefaultParam(value);

		return *this;
	}
};

template <typename BaseType>
class GDefineMetaMethod : public BaseType
{
private:
	typedef GDefineMetaMethod<BaseType> ThisType;

public:
	GDefineMetaMethod(meta_internal::GSharedMetaClass metaClass, GMetaMethod * method) : BaseType(metaClass, method) {
	}

	ThisType _default(const GVariant & value) {
		gdynamic_cast<GMetaMethod *>(this->currentItem)->addDefaultParam(value);

		return *this;
	}
};

template <typename BaseType>
class GDefineMetaEnum : public BaseType
{
private:
	typedef GDefineMetaEnum<BaseType> ThisType;

public:
	GDefineMetaEnum(meta_internal::GSharedMetaClass metaClass, GMetaEnum * metaEnum) : BaseType(metaClass, metaEnum) {
	}

	ThisType _element(const char * key, const GVariant & value) {
		(*gdynamic_cast<GMetaEnum *>(this->currentItem))(key, value);

		return *this;
	}

};

template <typename BaseType>
class GDefineMetaAnnotation : public BaseType
{
public:
	GDefineMetaAnnotation(meta_internal::GSharedMetaClass metaClass, GMetaAnnotation * annotation) : BaseType(metaClass, annotation) {
	}

	template <typename T>
	GDefineMetaAnnotation _element(const char * name, const T & value) {
		(*gdynamic_cast<GMetaAnnotation *>(this->currentItem))(name, value);

		return *this;
	}

};

template <typename BaseType>
class GDefineMetaField : public BaseType
{
public:
	GDefineMetaField(meta_internal::GSharedMetaClass metaClass, GMetaField * field) : BaseType(metaClass, field) {
	}

};


template <typename BaseType>
class GDefineMetaNonReflected : public BaseType
{
public:
	GDefineMetaNonReflected(meta_internal::GSharedMetaClass metaClass,
			GMetaNonReflectedItem * item) : BaseType(metaClass, item) {
	}

};

template <typename BaseType>
class GDefineMetaProperty : public BaseType
{
public:
	GDefineMetaProperty(meta_internal::GSharedMetaClass metaClass, GMetaProperty * prop) : BaseType(metaClass, prop) {
	}

};

template <typename BaseType>
class GDefineMetaOperator : public BaseType
{
private:
	typedef GDefineMetaOperator<BaseType> ThisType;

public:
	GDefineMetaOperator(meta_internal::GSharedMetaClass metaClass, GMetaOperator * op) : BaseType(metaClass, op) {
	}

	ThisType _default(const GVariant & value) {
		gdynamic_cast<GMetaOperator *>(this->currentItem)->addDefaultParam(value);

		return *this;
	}
};

template <typename BaseType>
class GDefineMetaInnerClass : public BaseType
{
public:
	GDefineMetaInnerClass(meta_internal::GSharedMetaClass metaClass, GMetaClass * inner) : BaseType(metaClass, inner) {
	}

};


class GDefineMetaInfo
{
public:
	explicit GDefineMetaInfo(meta_internal::GSharedMetaClass metaClass, bool dangling)
	: metaClass(metaClass), dangling(dangling) {
	}

	GDefineMetaInfo(const GDefineMetaInfo & other)
	: metaClass(other.metaClass), dangling(other.dangling) {
	}

	GDefineMetaInfo & operator = (const GDefineMetaInfo & other) {
		this->metaClass = other.metaClass;
		this->dangling = other.dangling;

		return *this;
	}

	GMetaClass * getMetaClass() const {
		return this->metaClass.get();
	}

	GMetaClass * takeMetaClass() {
		return this->metaClass.take();
	}

	bool isDangle() const {
		return this->dangling;
	}

protected:
	meta_internal::GSharedMetaClass metaClass;
	bool dangling;
};


template <typename ClassType GPP_REPEAT(MAX_BASE_COUNT, BASE_DEFAULT, GPP_EMPTY)>
class GDefineMetaClass;

template <typename ClassT, typename DerivedType>
class GDefineMetaCommon
{
public:
	typedef ClassT ClassType;

private:
	typedef GDefineMetaCommon<ClassType, DerivedType> ThisType;

public:
	GDefineMetaCommon(meta_internal::GSharedMetaClass metaClass, GMetaItem * currentItem)
: metaClass(metaClass), dangling(false), currentItem(currentItem) {
	}

	GDefineMetaCommon(const GDefineMetaCommon & other)
	: metaClass(other.metaClass), dangling(other.dangling), currentItem(other.currentItem) {
	}

	GDefineMetaCommon operator = (const GDefineMetaCommon & other) {
		this->metaClass = other.metaClass;
		this->dangling = other.dangling;
		this->currentItem = other.currentItem;

		return *this;
	}

	template <typename FT>
	GDefineMetaMethod<DerivedType> _method(const char * name, FT func) {
		return GDefineMetaMethod<DerivedType>(
				this->metaClass,
				this->metaClass->addMethod(GMetaMethod::newMethod<ClassType>(
						name, func, GMetaPolicyDefault()))
		);
	}

	template <typename FT, typename Policy>
	GDefineMetaMethod<DerivedType> _method(const char * name, FT func, const Policy & policy) {
		return GDefineMetaMethod<DerivedType>(
				this->metaClass,
				this->metaClass->addMethod(GMetaMethod::newMethod<ClassType>(
						name, func, policy))
		);
	}


	//scturner added type signature
	template <typename FT>
	GDefineMetaMethod<DerivedType> _methodEx(const char * name,
			const char * paramTypes, const char * nameSpace, int modifiers,
			FT func) {

		GMetaMethod * method = GMetaMethod::newMethod<ClassType>(
				name, func, GMetaPolicyDefault(), paramTypes, nameSpace);
		method->addModifier(modifiers);
		return GDefineMetaMethod<DerivedType>(
				this->metaClass,
				this->metaClass->addMethod(method)
		);
	}

	//scturner added type signature
	template <typename FT, typename Policy>
	GDefineMetaMethod<DerivedType> _methodEx(const char * name,
			const char * paramTypes, const char * nameSpace, int modifiers,
			FT func, const Policy & policy) {

		GMetaMethod * method = GMetaMethod::newMethod<ClassType>(
						name, func, policy, paramTypes, nameSpace);
		method->addModifier(modifiers);

		return GDefineMetaMethod<DerivedType>(
				this->metaClass,
				this->metaClass->addMethod(method)
		);
	}



	template <typename FT>
	GDefineMetaField<DerivedType> _field(const char * name, FT field) {
		return GDefineMetaField<DerivedType>(
				this->metaClass,
				//scturner
				this->metaClass->addField(new GMetaField(name, field, GMetaPolicyDefault(), NULL))
		);
	}

	template <typename FT, typename Policy>
	GDefineMetaField<DerivedType> _field(const char * name, FT field, const Policy & policy) {
		return GDefineMetaField<DerivedType>(
				this->metaClass,
				//scturner
				this->metaClass->addField(new GMetaField(name, field, policy, NULL))
		);
	}


	//scturner added type signature
	template <typename FT>
	GDefineMetaField<DerivedType> _fieldEx(const char * name,
			const char * paramTypes, const char * nameSpace, int modifiers,
			FT field) {
		GMetaField * gmField = new GMetaField(name, field,
				GMetaPolicyDefault(), paramTypes, nameSpace);
		gmField->addModifier(modifiers);
		return GDefineMetaField<DerivedType>(
				this->metaClass,
				this->metaClass->addField(gmField)
		);
	}
	//scturner added type signature
	template <typename FT, typename Policy>
	GDefineMetaField<DerivedType> _fieldEx(const char * name,
			const char * paramTypes, const char * nameSpace, int modifiers,
			FT field, const Policy & policy) {
		GMetaField * gmField = new GMetaField(name, field,
				policy, paramTypes, nameSpace);
		gmField->addModifier(modifiers);
		return GDefineMetaField<DerivedType>(
				this->metaClass,
				this->metaClass->addField(gmField)
		);
	}



	template <typename Getter, typename Setter>
	GDefineMetaProperty<DerivedType> _property(const char * name,
			const Getter & getter, const Setter & setter) {
		return GDefineMetaProperty<DerivedType>(
				this->metaClass,
				this->metaClass->addProperty(new GMetaProperty(name, getter, setter,
						GMetaPolicyDefault(), ""))
		);
	}

	template <typename Getter, typename Setter, typename Policy>
	GDefineMetaProperty<DerivedType> _property(const char * name,
			const Getter & getter, const Setter & setter, const Policy & policy) {
		return GDefineMetaProperty<DerivedType>(
				this->metaClass,
				this->metaClass->addProperty(new GMetaProperty(name, getter,
						setter, policy, ""))
		);
	}



	//supported yet?
	//scturner added type signature
	template <typename Getter, typename Setter>
	GDefineMetaProperty<DerivedType> _propertyEx(const char * name,
			const char * paramType, const char * nameSpace, int modifiers,
			const Getter & getter, const Setter & setter) {
		return GDefineMetaProperty<DerivedType>(
				this->metaClass,
				this->metaClass->addProperty(new GMetaProperty(name,
						getter, setter, GMetaPolicyDefault()))
		);
	}
	//scturner added type signature
	template <typename Getter, typename Setter, typename Policy>
	GDefineMetaProperty<DerivedType> _propertyEx(const char * name,
			const char * paramType, const char * nameSpace, int modifiers,
			const Getter & getter, const Setter & setter, const Policy & policy) {
		return GDefineMetaProperty<DerivedType>(
				this->metaClass,
				this->metaClass->addProperty(new GMetaProperty(name,
						getter, setter, policy))
		);
	}



	template <typename FT, typename Creator>
	GDefineMetaOperator<DerivedType> _operator(const Creator & creator) {
		return GDefineMetaOperator<DerivedType>(
				this->metaClass,
				this->metaClass->addOperator(new GMetaOperator(creator.template create<ClassType, FT>(GMetaPolicyDefault())))
		);
	}

	template <typename FT, typename Creator, typename Policy>
	GDefineMetaOperator<DerivedType> _operator(const Creator & creator, const Policy & policy) {
		return GDefineMetaOperator<DerivedType>(
				this->metaClass,
				this->metaClass->addOperator(new GMetaOperator(creator.template create<ClassType, FT>(policy)))
		);
	}


	//scturner added type signature
	template <typename FT, typename Creator>
	GDefineMetaOperator<DerivedType> _operatorEx(const char * paramTypes,
			const char * nameSpace, int modifiers, const Creator & creator) {
		GMetaOperator * gmOperator = new GMetaOperator(
				creator.template create<ClassType, FT>(GMetaPolicyDefault(),
						paramTypes, nameSpace));
		gmOperator->addModifier(modifiers);
		return GDefineMetaOperator<DerivedType>(
				this->metaClass,
				this->metaClass->addOperator(gmOperator)
		);
	}

	//scturner added type signature
	template <typename FT, typename Creator, typename Policy>
	GDefineMetaOperator<DerivedType> _operatorEx(const char * paramTypes,
			const char * nameSpace, int modifiers, const Creator & creator,
			const Policy & policy) {
		GMetaOperator * gmOperator = new GMetaOperator(
				creator.template create<ClassType, FT>(policy,
						paramTypes, nameSpace));
		gmOperator->addModifier(modifiers);
		return GDefineMetaOperator<DerivedType>(
				this->metaClass,
				this->metaClass->addOperator(gmOperator)
		);
	}



	template <typename T>
	GDefineMetaEnum<DerivedType> _enum(const char * name,
			const char * nameSpace, int modifiers) {
		GMetaEnum * gmEnum = new GMetaEnum(name,
				createMetaType<T>(), nameSpace);
		gmEnum->addModifier(modifiers);
		return GDefineMetaEnum<DerivedType>(
				this->metaClass,
				this->metaClass->addEnum(gmEnum)
		);
	}

	template <typename MetaClass>
	GDefineMetaInnerClass<DerivedType> _class(MetaClass defineClass) {
		return this->_class(defineClass.getMetaInfo());
	}

	GDefineMetaInnerClass<DerivedType> _class(GDefineMetaInfo metaInfo) {
		if(metaInfo.isDangle()) {
			metaInfo.getMetaClass()->extractTo(this->metaClass.get());
			return GDefineMetaInnerClass<DerivedType>(
					this->metaClass,
					this->metaClass.get()
			);
		}
		else {
			return GDefineMetaInnerClass<DerivedType>(
					this->metaClass,
					this->metaClass->addClass(metaInfo.takeMetaClass())
			);
		}
	}

	GDefineMetaAnnotation<DerivedType> _annotation(const char * name, const char * nameSpace) {
		return GDefineMetaAnnotation<DerivedType>(
				this->metaClass,
				this->currentItem->addItemAnnotation(new GMetaAnnotation(name, nameSpace))
		);
	}



	//scturner added type signature
	/**
	 * Creates a non-reflected field in the class.
	 *
	 * @param name Name of the field (i.e. x)
	 * @param signature Signature of the field (i.e. int x)
	 * @param vis Visibility of the item (one of mPrivate = 1, mProtected = 2,
	 *	 mPublic = 4)
	 * @param nameSpace namespace this item is in
	 */
	GDefineMetaNonReflected<DerivedType> _fieldNR(const char * name,
			const char * signature, GMetaVisibility vis, const char * nameSpace,
			int modifiers) {
		GMetaNonReflectedItem * nrItem = new GMetaNonReflectedItem(
								name, mcatField, vis, signature, nameSpace);
		nrItem->addModifier(modifiers);
		return GDefineMetaNonReflected<DerivedType>(
				this->metaClass,
				this->metaClass->addNonReflected(nrItem)
		);
	}


	/**
	 * Creates a non-reflected method in the class.
	 *
	 * @param name Name of the field (i.e. foo)
	 * @param signature Signature of the field (i.e. int foo(double))
	 * @param vis Visibility of the item (one of mPrivate = 1, mProtected = 2,
	 *	 mPublic = 4)
	 * @param nameSpace namespace this item is in
	 */
	GDefineMetaNonReflected<DerivedType> _methodNR(const char * name,
			const char * signature, GMetaVisibility vis, const char * nameSpace,
			int modifiers) {
		GMetaNonReflectedItem * nrItem = new GMetaNonReflectedItem(
				name, mcatMethod, vis, signature, nameSpace);
		nrItem->addModifier(modifiers);
		return GDefineMetaNonReflected<DerivedType>(
				this->metaClass,
				this->metaClass->addNonReflected(nrItem)
		);
	}


	/**
	 * Creates a non-reflected property in the class.
	 *
	 * @param name Name of the property
	 * @param signature Signature of the property
	 * @param vis Visibility of the item (one of mPrivate = 1, mProtected = 2,
	 *	 mPublic = 4)
	 * @param nameSpace namespace this item is in
	 */
	GDefineMetaNonReflected<DerivedType> _propertyNR(const char * name,
			const char * signature, GMetaVisibility vis, const char * nameSpace,
			int modifiers) {
		GMetaNonReflectedItem * nrItem = new GMetaNonReflectedItem(
				name, mcatProperty, vis, signature, nameSpace);
		nrItem->addModifier(modifiers);

		return GDefineMetaNonReflected<DerivedType>(
				this->metaClass,
				this->metaClass->addNonReflected(nrItem)
		);
	}


	/**
	 * Creates a non-reflected enum in the class.
	 *
	 * @param name Name of the enum (i.e. myEnum)
	 * @param signature Signature of the enum (i.e. myEnum {first, second = 4, third})
	 * @param vis Visibility of the item (one of mPrivate = 1, mProtected = 2,
	 *	 mPublic = 4)
	 * @param nameSpace namespace this item is in
	 */
	GDefineMetaNonReflected<DerivedType> _enumNR(const char * name,
			const char * signature, GMetaVisibility vis, const char * nameSpace,
			int modifiers) {
		GMetaNonReflectedItem * nrItem = new GMetaNonReflectedItem(
				name, mcatEnum, vis, signature, nameSpace);
		nrItem->addModifier(modifiers);

		return GDefineMetaNonReflected<DerivedType>(
				this->metaClass,
				this->metaClass->addNonReflected(nrItem)
		);
	}


	/**
	 * Creates a non-reflected operator in the class.
	 *
	 * Not used. Use _methodNR instead
	 *
	 * @param name Name of the operator (i.e. operator+)
	 * @param signature Signature of the operator (i.e. int operator+(int, int))
	 * @param vis Visibility of the item (one of mPrivate = 1, mProtected = 2,
	 *	 mPublic = 4)
	 * @param nameSpace namespace this item is in
	 */
	GDefineMetaNonReflected<DerivedType> _operatorNR(const char * name,
			const char * signature, GMetaVisibility vis, const char * nameSpace,
			int modifiers) {
		GMetaNonReflectedItem * nrItem = new GMetaNonReflectedItem(
				name, mcatOperator, vis, signature, nameSpace);
		nrItem->addModifier(modifiers);

		return GDefineMetaNonReflected<DerivedType>(
				this->metaClass,
				this->metaClass->addNonReflected(nrItem)
		);
	}




	//  mcatConstructor
	//mcatClass

	//mcatBaseClass


protected:
	meta_internal::GSharedMetaClass metaClass;
	bool dangling;
	GMetaItem * currentItem;
};


template <typename ClassType GPP_REPEAT_TAIL_PARAMS(MAX_BASE_COUNT, typename BaseType)>
class GDefineMetaClass : public GDefineMetaCommon<ClassType, GDefineMetaClass<ClassType GPP_REPEAT_TAIL_PARAMS(MAX_BASE_COUNT, BaseType)> >
{
private:
	typedef GDefineMetaClass<ClassType GPP_REPEAT_TAIL_PARAMS(MAX_BASE_COUNT, BaseType)> ThisType;
	typedef GDefineMetaCommon<ClassType, ThisType > super;

public:
	static ThisType define(const char * className, const char * nameSpace,
			int modifiers, int baseModifiers[]) {
		ThisType c;
		c.init(className, NULL, true, GMetaPolicyDefault(), nameSpace,
				modifiers, baseModifiers);
		return c;
	}

	static ThisType declare(const char * className, const char * nameSpace,
			int modifiers, int baseModifiers[]) {
		ThisType c;
		c.init(className, NULL, false, GMetaPolicyDefault(), nameSpace,
				modifiers, baseModifiers);
		return c;
	}

	static ThisType fromMetaClass(GMetaClass * metaClass) {
		return ThisType(metaClass);
	}

	static ThisType lazy(const char * className, void (*reg)(ThisType define)) {
		GASSERT(reg != NULL);

		ThisType c;
		meta_internal::GLazyDefineClassHelper<ThisType>::registerAddress = reg;
		c.init(className, &meta_internal::GLazyDefineClassHelper<ThisType>::metaRegister, true, GMetaPolicyDefault());
		return c;
	}

	static ThisType lazyDeclare(const char * className, void (*reg)(ThisType define)) {
		GASSERT(reg != NULL);

		ThisType c;
		meta_internal::GLazyDefineClassHelper<ThisType>::registerAddress = reg;
		c.init(className, &meta_internal::GLazyDefineClassHelper<ThisType>::metaRegister, false, GMetaPolicyDefault());
		return c;
	}

	template <typename P>
	struct Policy {
		static ThisType define(const char * className, const char * nameSpace,
				int modifiers, int baseModifiers[]) {
			ThisType c;
			c.init(className, NULL, true, P(), nameSpace, modifiers, baseModifiers);
			return c;
		}

		static ThisType declare(const char * className, const char * nameSpace,
				int modifiers, int baseModifiers[]) {
			ThisType c;
			c.init(className, NULL, false, P(), nameSpace, modifiers, baseModifiers);
			return c;
		}

		static ThisType lazy(const char * className, void (*reg)(ThisType define)) {
			GASSERT(reg != NULL);

			ThisType c;
			meta_internal::GLazyDefineClassHelper<ThisType>::registerAddress = reg;
			c.init(className, &meta_internal::GLazyDefineClassHelper<ThisType>::metaRegister, true, P());
			return c;
		}

		static ThisType lazyDeclare(const char * className, void (*reg)(ThisType define)) {
			GASSERT(reg != NULL);

			ThisType c;
			meta_internal::GLazyDefineClassHelper<ThisType>::registerAddress = reg;
			c.init(className, &meta_internal::GLazyDefineClassHelper<ThisType>::metaRegister, false, P());
			return c;
		}
	};


protected:
	GDefineMetaClass() : super(meta_internal::GSharedMetaClass(), NULL) {
	}

	explicit GDefineMetaClass(GMetaClass * metaClass) : super(meta_internal::GSharedMetaClass(metaClass), metaClass) {
		this->takeMetaClass();
	}

	GDefineMetaClass(GMetaClass * metaClass, GMetaItem * currentItem) : super(meta_internal::GSharedMetaClass(metaClass), currentItem) {
		this->takeMetaClass();
	}

	GDefineMetaClass(meta_internal::GSharedMetaClass metaClass, GMetaItem * currentItem) : super(metaClass, currentItem) {
	}

public:
	template <typename BaseType>
	ThisType _base(int modifiers) {
		this->metaClass->template addBaseClass<ClassType, BaseType>(modifiers);

		return *this;
	}

	template <typename FT>
	GDefineMetaConstructor<ThisType> _constructor() {
		return GDefineMetaConstructor<ThisType>(
				this->metaClass,
				this->metaClass->addConstructor(GMetaConstructor::newConstructor<ClassType, FT>(GMetaPolicyDefault()))
		);
	}

	template <typename FT, typename Policy>
	GDefineMetaConstructor<ThisType> _constructor(const Policy & policy) {
		return GDefineMetaConstructor<ThisType>(
				this->metaClass,
				this->metaClass->addConstructor(GMetaConstructor::newConstructor<ClassType, FT>(policy))
		);
	}

	template <typename FT>
	GDefineMetaConstructor<ThisType> _constructor(const FT & func) {
		return GDefineMetaConstructor<ThisType>(
				this->metaClass,
				this->metaClass->addConstructor(GMetaConstructor::newConstructor<ClassType, FT>(func, GMetaPolicyDefault()))
		);
	}

	template <typename FT, typename Policy>
	GDefineMetaConstructor<ThisType> _constructor(const FT & func, const Policy & policy) {
		return GDefineMetaConstructor<ThisType>(
				this->metaClass,
				this->metaClass->addConstructor(GMetaConstructor::newConstructor<ClassType, FT>(func, policy))
		);
	}




	//scturner added type signature
	template <typename FT>
	GDefineMetaConstructor<ThisType> _constructorEx(const char * paramTypes,
			const char * nameSpace, int modifiers) {
		GMetaConstructor * c = GMetaConstructor::newConstructor<ClassType, FT>(
				GMetaPolicyDefault(), paramTypes, nameSpace);
		c->addModifier(modifiers);
		return GDefineMetaConstructor<ThisType>(
				this->metaClass,
				this->metaClass->addConstructor(c)
		);
	}
	//scturner added type signature
	template <typename FT, typename Policy>
	GDefineMetaConstructor<ThisType> _constructorEx(const char * paramTypes,
			const char * nameSpace, int modifiers, const Policy & policy) {
		GMetaConstructor * c = GMetaConstructor::newConstructor<ClassType, FT>(
				policy, paramTypes, nameSpace);
		c->addModifier(modifiers);
		return GDefineMetaConstructor<ThisType>(
				this->metaClass,
				this->metaClass->addConstructor(c)
		);
	}
	//scturner added type signature
	template <typename FT>
	GDefineMetaConstructor<ThisType> _constructorEx(const char * paramTypes,
			const char * nameSpace, int modifiers, const FT & func) {
		GMetaConstructor * c = GMetaConstructor::newConstructor<ClassType, FT>(
				func, GMetaPolicyDefault(),
				paramTypes, nameSpace);
		c->addModifier(modifiers);
		return GDefineMetaConstructor<ThisType>(
				this->metaClass,
				this->metaClass->addConstructor(c)
		);
	}
	//scturner added type signature
	template <typename FT, typename Policy>
	GDefineMetaConstructor<ThisType> _constructorEx(const char * paramTypes,
			const char * nameSpace, int modifiers, const FT & func,
			const Policy & policy) {
		GMetaConstructor * c = GMetaConstructor::newConstructor<ClassType, FT>(
				func, policy, paramTypes, nameSpace);
		c->addModifier(modifiers);
		return GDefineMetaConstructor<ThisType>(
				this->metaClass,
				this->metaClass->addConstructor(c)
		);
	}


	GDefineMetaInfo getMetaInfo() const {
		return GDefineMetaInfo(this->metaClass, this->dangling);
	}

	GMetaClass * getMetaClass() const {
		return this->metaClass.get();
	}

	GMetaClass * takeMetaClass() {
		return this->metaClass.take();
	}


	/**
	 * Creates a non-reflected base class in the class.
	 *
	 *
	 * @param name Name of the base class (i.e. string)
	 * @param vis Visibility of the item (one of mPrivate = 1, mProtected = 2,
	 *	 mPublic = 4)
	 * @param nameSpace namespace this item is in
	 */
	GDefineMetaNonReflected<ThisType> _baseNR(const char * name,
			GMetaVisibility vis, const char * nameSpace, int modifiers) {
		GMetaNonReflectedItem * nrItem = new GMetaNonReflectedItem(
				name, mcatBaseClass, vis, name, nameSpace);
		nrItem->addModifier(modifiers);


		return GDefineMetaNonReflected<ThisType>(
				this->metaClass,
				this->metaClass->addNonReflected(nrItem)
		);
	}


	/**
	 * Creates a non-reflected constructor in the class.
	 *
	 *
	 * @param signature Signature of the constructor (i.e. (int, double))
	 * @param vis Visibility of the item (one of mPrivate = 1, mProtected = 2,
	 *	 mPublic = 4)
	 * @param nameSpace namespace this item is in
	 */
	GDefineMetaNonReflected<ThisType> _constructorNR(
			const char * signature, GMetaVisibility vis, const char * nameSpace,
			int modifiers) {

//		CPGF_TRACE("constructor NR \r\n" )
		std::string sig = this->metaClass->getName() + signature;
//		CPGF_TRACE("constructor NR " << sig << "\r\n")
		GMetaNonReflectedItem * nrItem = new GMetaNonReflectedItem(
				this->metaClass->getName().c_str(), mcatConstructor, vis,
				sig.c_str(), nameSpace);
		nrItem->addModifier(modifiers);
//		CPGF_TRACE("constructor NR mods\r\n")
		return GDefineMetaNonReflected<ThisType>(
				this->metaClass,
				this->metaClass->addNonReflected(nrItem)
		);
	}


	/**
	 * Creates a non-reflected inner class in the class.
	 *
	 * @param name Name of the inner class (i.e. iClass)
	 * @param vis Visibility of the item (one of mPrivate = 1, mProtected = 2,
	 *	 mPublic = 4)
	 * @param nameSpace namespace this item is in
	 */
	GDefineMetaNonReflected<ThisType> _innerClassNR(const char * name,
			GMetaVisibility vis, const char * nameSpace, int modifiers) {
		GMetaNonReflectedItem * nrItem = new GMetaNonReflectedItem(
				name, mcatClass, vis, name, nameSpace);
		nrItem->addModifier(modifiers);

		return GDefineMetaNonReflected<ThisType>(
				this->metaClass,
				this->metaClass->addNonReflected(nrItem)
		);
	}



protected:
	typedef typename cpgf::TypeList_Make<GPP_REPEAT_PARAMS(MAX_BASE_COUNT, BaseType)>::Result BaseListType;

	template <typename P>
	void init(const char * className, void (*reg)(GMetaClass *),
			bool addToGlobal, const P & policy, const char * nameSpace,
			int modifiers, int baseModifiers[]) {
		GMetaClass * classToAdd = NULL;

		if(addToGlobal) {
			classToAdd = const_cast<GMetaClass *>(getGlobalMetaClass()->doGetClass(className));
		}

		if(classToAdd == NULL) {
			classToAdd = new GMetaClass(
					//TODO add modifier here
					(ClassType *)0, meta_internal::doMakeSuperList<BaseListType,
						ClassType>(baseModifiers),
					className, reg, policy, nameSpace
			);

			if(addToGlobal) {
				getGlobalMetaClass()->addClass(classToAdd);
			}
		}

		classToAdd->addModifier(modifiers);

		this->metaClass.reset(classToAdd);
		if(addToGlobal) {
			this->metaClass.take();
		}

		this->currentItem = classToAdd;
	}

private:
	template <typename DefineClass>
	friend struct meta_internal::GLazyDefineClassHelper;
};


template <typename ClassType>
class GDefineMetaDangle : public GDefineMetaCommon<ClassType, GDefineMetaDangle<ClassType> >
{
private:
	typedef GDefineMetaDangle ThisType;
	typedef GDefineMetaCommon<ClassType, GDefineMetaDangle<ClassType> > super;

public:
	static ThisType dangle() {
		ThisType c;
		c.init();
		return c;
	}

	template <typename FT>
	GDefineMetaConstructor<ThisType> _constructor() {
		return GDefineMetaConstructor<ThisType>(
				this->metaClass,
				this->metaClass->addConstructor(GMetaConstructor::newConstructor<ClassType, FT>(GMetaPolicyDefault()))
		);
	}

	template <typename FT, typename Policy>
	GDefineMetaConstructor<ThisType> _constructor(const Policy & policy) {
		return GDefineMetaConstructor<ThisType>(
				this->metaClass,
				this->metaClass->addConstructor(GMetaConstructor::newConstructor<ClassType, FT>(policy))
		);
	}

	template <typename FT>
	GDefineMetaConstructor<ThisType> _constructor(const FT & func) {
		return GDefineMetaConstructor<ThisType>(
				this->metaClass,
				this->metaClass->addConstructor(GMetaConstructor::newConstructor<ClassType, FT>(func, GMetaPolicyDefault()))
		);
	}

	template <typename FT, typename Policy>
	GDefineMetaConstructor<ThisType> _constructor(const FT & func, const Policy & policy) {
		return GDefineMetaConstructor<ThisType>(
				this->metaClass,
				this->metaClass->addConstructor(GMetaConstructor::newConstructor<ClassType, FT>(func, policy))
		);
	}

	GDefineMetaInfo getMetaInfo() const {
		return GDefineMetaInfo(this->metaClass, this->dangling);
	}

	GMetaClass * getMetaClass() const {
		return this->metaClass.get();
	}

	GMetaClass * takeMetaClass() {
		return this->metaClass.take();
	}

protected:
	GDefineMetaDangle() : super(meta_internal::GSharedMetaClass(), NULL) {
	}

	explicit GDefineMetaDangle(GMetaClass * metaClass) : super(meta_internal::GSharedMetaClass(metaClass, false), metaClass) {
	}

	GDefineMetaDangle(GMetaClass * metaClass, GMetaItem * currentItem) : super(meta_internal::GSharedMetaClass(metaClass), currentItem) {
	}

	GDefineMetaDangle(meta_internal::GSharedMetaClass metaClass, GMetaItem * currentItem) : super(metaClass, currentItem) {
	}

protected:
	void init() {
		this->dangling = true;

		GMetaClass * metaClass = new GMetaClass((ClassType *)0,
				new meta_internal::GMetaSuperList, "", NULL,
				GMetaPolicyDefault(), "");

		this->metaClass.reset(metaClass);
		this->currentItem = metaClass;
	}

private:
	template <typename DefineClass>
	friend struct meta_internal::GLazyDefineClassHelper;

};


class GDefineMetaGlobal : public GDefineMetaCommon<void, GDefineMetaGlobal >
{
private:
	typedef GDefineMetaCommon<void, GDefineMetaGlobal > super;
	typedef GDefineMetaGlobal ThisType;

public:
	GDefineMetaGlobal() : super(meta_internal::GSharedMetaClass(getGlobalMetaClass(), false), getGlobalMetaClass()) {
	}

	explicit GDefineMetaGlobal(GMetaClass * metaClass) : super(meta_internal::GSharedMetaClass(metaClass, false), metaClass) {
	}

	GDefineMetaGlobal(meta_internal::GSharedMetaClass metaClass, GMetaItem * currentItem) : super(meta_internal::GSharedMetaClass(metaClass.take(), false), currentItem) {
	}

	void setName(const char * name) {
		this->metaClass->rebindName(name);
	}

};


typedef GDefineMetaClass<void> GDefineMetaNamespace;

typedef GDefineMetaDangle<void> GDefineMetaGlobalDangle;


} // namespace cpgf



#undef MAX_BASE_COUNT
#undef BASE_DEFAULT


#endif

