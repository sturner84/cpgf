/*
 * gmetanonreflected.h
 *
 * Stores information about non-reflected items (private items, base classes
 *  that aren't being reflected, etc.)
 *
 *  Created on: Jun 6, 2014
 *      Author: scturner
 */

#ifndef GMETANONREFLECTED_H_
#define GMETANONREFLECTED_H_

#include "cpgf/gmetacommon.h"
#include <string>

namespace cpgf {


/**
 * Stores information about non-reflected items (private items, base classes
 *  that aren't being reflected, etc.)
 */
class GMetaNonReflectedItem : public GMetaItem {
private:
	std::string itemSignature;
	GMetaCategory itemCategory;
	GMetaVisibility visibility;
public:
	/**
	 * Constructs a non-reflected item.
	 *
	 * @param name Name of the item
	 * @param category The category of the item. This should be one of
	 * 	mcatField, mcatProperty, mcatMethod, mcatEnum, mcatOperator,
	 *  mcatConstructor, mcatClass
	 * @param signature The full signature of the item (method signature, enum
	 *   with values, field signature, etc.)
	 * @param nameSpace namespace of the item.
	 */
	GMetaNonReflectedItem(const char * name, GMetaCategory category,
			GMetaVisibility vis, const char * signature,
			const char * nameSpace) :
				GMetaItem(name, createMetaType<void>(), mcatNonReflected,
						nameSpace), itemSignature(signature),
						itemCategory(category),
						visibility(vis)  {	}

	/**
	 * Destructor
	 */
	virtual ~GMetaNonReflectedItem() {}

	/**
	 * Gets the signature for this item.
	 *
	 * @return Signature
	 */
	const char * getSignature() const { return itemSignature.c_str(); }

	/**
	 * Gets the category for the item.  Note: getCategory returns
	 * mcatNonReflected.  This method returns the actual category for the
	 * item.
	 *
	 * @return Category for the item.
	 */
	GMetaCategory getItemCategory() const { return itemCategory; }


	/**
	 * Gets the visibility of this item
	 * @return The visibility (private, protected, public)
	 */
	GMetaVisibility getVisibility() const { return visibility; }


	virtual GMetaExtendType getItemExtendType(uint32_t flags) const {
		return createMetaExtendType<std::string>(flags);
	}
};

} //end namespace cpgf

#endif /* GMETANONREFLECTED_H_ */
