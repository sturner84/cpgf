/*
 * gtrace.h
 *
 * @brief Provides basic tracing features for debugging development code.
 *
 *  Created on: Jun 5, 2014
 *      Author: scturner
 */

#ifndef GTRACE_H_
#define GTRACE_H_

#include "cpgf/gconfig.h"
#include <iostream>
#include <sstream>
#include <fstream>

namespace cpgf {

/**
 * @brief Provides basic tracing feature for debugging development code.
 *
 */
class Tracer {
private:
	std::ostream * traceStream;
	std::stringstream buffer;
	std::string fileName;
	std::fstream fileStream;

	static Tracer instance;

	Tracer();
public:

	/**
	 * Closes the tracer
	 */
	~Tracer();

	/**
	 * Gets a singleton.
	 * @return Tracer object
	 */
	static Tracer & getTracer();

	/**
	 * Returns a reference to a ostream that can be written to. This does not
	 * commit the data to the file/console.  Use flush for that.
	 *
	 * @return ostream that can be written to.
	 */
	std::ostream & trace();

	/**
	 * Flushes the data to the file/console.
	 */
	void flush();

	/**
	 * Switches the Tracer to use the console (cout) for output.
	 * This is the default.
	 *
	 * @return true if the switch could be made
	 */
	bool toConsole();

	/**
	 * Switches the Tracer to use a file for output.
	 * A file name of "" is equivalent of calling toConsole.
	 *
	 * @param filename The name of the file to store the output in
	 * @return true if the switch could be made
	 */
	bool toFile(std::string filename);

	/**
	 * Returns true if the tracing is being output to cout
	 *
	 * @return true if the tracing is being output to cout
	 */
	bool isTracingToConsole();


	/**
	 * Returns true if the tracing is being written to a file
	 *
	 * @return true if the tracing is being output to a file
	 */
	bool isTracingToFile();

	/**
	 * Gets the name if the file the output is being stored in.
	 * If isTracingToFile is false, this returns ""
	 */
	std::string getTracingFileName();


	/**
	 * Strips a filename of its path
	 * @param str filename with path
	 * @return filename without path
	 */
	static std::string strip_path(const char * str);
};


#ifdef G_TRACE

#define STRIP_PATH(S)

#define CPGF_TRACE(N) \
	cpgf::Tracer::getTracer().trace() << cpgf::Tracer::strip_path(__FILE__) \
	<< ":" << __LINE__ << " " << N ; \
	cpgf::Tracer::getTracer().flush();

#else

std::ostream & getNullStream();

#define CPGF_TRACE(N) getNullStream() << N ;
#endif


} //namespace cpgf

#endif /* GTRACE_H_ */
