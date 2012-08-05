#ifndef __SIMPLEOVERRIDEFROMSCRIPT_H
#define __SIMPLEOVERRIDEFROMSCRIPT_H

#include <string>

class SimpleOverride
{
public:
	explicit SimpleOverride(int n);

	virtual int getValue();

	virtual std::string getName() { return ""; }

//private:
	int n;
};


#endif