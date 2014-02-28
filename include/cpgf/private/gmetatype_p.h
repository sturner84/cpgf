#ifndef CPGF_GMETATYPE_P_H
#define CPGF_GMETATYPE_P_H

#if defined(_MSC_VER)
#pragma warning(push)
#pragma warning(disable:4127) // warning C4127: conditional expression is constant
#endif


namespace meta_internal {

template <typename T>
struct RemoveAllPointers
{
	typedef T Result;
};

template <typename T>
struct RemoveAllPointers <T *>
{
	typedef typename RemoveAllPointers<T>::Result Result;
};

template <typename T>
struct StripBaseType
{
	typedef
		typename RemoveConstVolatile<
			typename RemoveAllPointers<
				typename RemoveReference<
					typename RemoveConstVolatile<T>::Result
				>::Result
			>::Result
		>::Result
	Result;
};

template <typename T>
struct StripNonPointer
{
	typedef
		typename RemoveConstVolatile<
			typename RemoveReference<
				typename RemoveConstVolatile<T>::Result
			>::Result
		>::Result
	Result;
};


const unsigned int mtFlagIsConst = 1 << 0;
const unsigned int mtFlagIsVolatile = 1 << 1;
const unsigned int mtFlagIsConstVolatile = 1 << 2;
const unsigned int mtFlagIsReference = 1 << 3;
const unsigned int mtFlagIsReferenceToConst = 1 << 4;
const unsigned int mtFlagIsReferenceToVolatile = 1 << 5;
const unsigned int mtFlagIsReferenceToConstVolatile = 1 << 6;
const unsigned int mtFlagIsPointer = 1 << 7;
const unsigned int mtFlagIsPointerToConst = 1 << 8;
const unsigned int mtFlagIsPointerToVolatile = 1 << 9;
const unsigned int mtFlagIsPointerToConstVolatile = 1 << 10;
const unsigned int mtFlagIsFunction = 1 << 11;
const unsigned int mtFlagIsConstFunction = 1 << 12;
const unsigned int mtFlagIsVolatileFunction = 1 << 13;
const unsigned int mtFlagIsConstVolatileFunction = 1 << 14;
const unsigned int mtFlagBaseIsClass = 1 << 15;
const unsigned int mtFlagBaseIsArray = 1 << 16;
const unsigned int mtFlagBaseIsStdString = 1 << 17;
const unsigned int mtFlagBaseIsStdWideString = 1 << 18;

template <typename T>
struct GMetaTypeDeduce
{
private:
	typedef typename StripBaseType<T>::Result BaseType;
	//scturner
	#ifdef G_CONVERT_ARRAYS_TO_POINTERS
		typedef typename ArrayToPointer<typename StripNonPointer<T>::Result>::Result ArrayAsPointerNoCV;
	#endif
	typedef typename StripNonPointer<T>::Result PointerNoCV;
	typedef typename RemoveConstVolatile<T>::Result NoCV;

public:
	enum {
		Flags = 0
			| (IsConst<T>::Result ? mtFlagIsConst : 0)
			| (IsVolatile<T>::Result ? mtFlagIsVolatile : 0)
			| (IsConstVolatile<T>::Result ? mtFlagIsConstVolatile : 0)
			//scturner
#ifdef G_CONVERT_ARRAYS_TO_POINTERS
			| ((PointerDimension<ArrayAsPointerNoCV>::Result > 0) ? mtFlagIsPointer : 0)
			| (IsConst<typename RemovePointer<ArrayAsPointerNoCV>::Result>::Result ? mtFlagIsPointerToConst : 0)
			| (IsVolatile<typename RemovePointer<ArrayAsPointerNoCV>::Result>::Result ? mtFlagIsPointerToVolatile : 0)
			| (IsConstVolatile<typename RemovePointer<ArrayAsPointerNoCV>::Result>::Result ? mtFlagIsPointerToConstVolatile : 0)
#else
			| ((PointerDimension<PointerNoCV>::Result > 0) ? mtFlagIsPointer : 0)
			| (IsConst<typename RemovePointer<PointerNoCV>::Result>::Result ? mtFlagIsPointerToConst : 0)
			| (IsVolatile<typename RemovePointer<PointerNoCV>::Result>::Result ? mtFlagIsPointerToVolatile : 0)
			| (IsConstVolatile<typename RemovePointer<PointerNoCV>::Result>::Result ? mtFlagIsPointerToConstVolatile : 0)
#endif
			| (IsReference<NoCV>::Result ? mtFlagIsReference : 0)
			| (IsConst<typename RemoveReference<NoCV>::Result>::Result ? mtFlagIsReferenceToConst : 0)
			| (IsVolatile<typename RemoveReference<NoCV>::Result>::Result ? mtFlagIsReferenceToVolatile : 0)
			| (IsConstVolatile<typename RemoveReference<NoCV>::Result>::Result ? mtFlagIsReferenceToConstVolatile : 0)

			| (IsFunction<T>::Result ? mtFlagIsFunction : 0)
			| (GFunctionTraits<T>::IsConst ? mtFlagIsConstFunction : 0)
			| (GFunctionTraits<T>::IsVolatile ? mtFlagIsVolatileFunction : 0)
			| (GFunctionTraits<T>::IsConstVolatile ? mtFlagIsConstVolatileFunction : 0)

			| (IsClass<BaseType>::Result ? mtFlagBaseIsClass : 0)
			| (IsArray<BaseType>::Result ? mtFlagBaseIsArray : 0)
			
			| (IsSameType<BaseType, std::string>::Result ? mtFlagBaseIsStdString : 0)
			| (IsSameType<BaseType, std::wstring>::Result ? mtFlagBaseIsStdWideString : 0)
	};

	static GTypeInfo getBaseType() {
		return gTypeId<BaseType>();
	}
};

template <typename T>
void deduceMetaTypeData(GMetaTypeData * data)
{
	//	//std::cout << "deducing\n";
	//	fprintf(stdout, "%s\n", "deducing");
	data->baseName = NULL;
	data->flags = GMetaTypeDeduce<T>::Flags;
	//	fprintf(stdout, "%s %d\n", "Flags", data->flags);
	//	fflush(stdout);
	deduceVariantType<T>(data->typeData);
	
	if(IsArray<typename StripBaseType<T>::Result>::Result) {
		//TODO scturner fix this
#ifdef G_CONVERT_ARRAYS_TO_POINTERS
		//		fprintf(stdout, "%s\n", "Pointers");
		vtSetPointers(data->typeData, PointerDimension<typename ArrayToPointer<typename StripNonPointer<T>::Result>::Result>::Result);
#else
		vtSetPointers(data->typeData, PointerDimension<typename StripNonPointer<T>::Result>::Result);
#endif
	}
}

} // namespace meta_internal



#if defined(_MSC_VER)
#pragma warning(pop)
#endif



#endif
