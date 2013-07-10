#ifndef __BUILDERCONSTRUCTOR_H
#define __BUILDERCONSTRUCTOR_H

#include "builderinvokable.h"

namespace metagen {


class CppConstructor;

class BuilderConstructor : public BuilderInvokable
{
private:
	typedef BuilderInvokable super;
	
public:
	explicit BuilderConstructor(const CppItem * cppItem);
	virtual ~BuilderConstructor();
	
	const CppConstructor * getCppConstructor() const;
	
protected:
	virtual void doWriteMetaData(BuilderFileWriter * writer);
};


} // namespace metagen


#endif
