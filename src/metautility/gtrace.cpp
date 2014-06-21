/*
 * gtrace.cpp
 *
 *  Created on: Jun 6, 2014
 *      Author: scturner
 */


#include "cpgf/metautility/gtrace.h"

namespace cpgf {


#ifndef G_TRACE

std::ostream & nullStream(NULL);

std::ostream & getNullStream() {
	return nullStream;
}


#endif



Tracer Tracer::instance;


Tracer::Tracer() {
	traceStream = &std::cout;
	fileName = "";
}

/**
 * Closes the tracer
 */
Tracer::~Tracer() {
	if (isTracingToFile()) {
		fileStream.close();
	}
}


/**
 * Gets a singleton.
 * @return Tracer object
 */
Tracer & Tracer::getTracer() {
	return instance;
}

/**
 * Returns a reference to a ostream that can be written to. This does not
 * commit the data to the file/console.  Use flush for that.
 *
 * @return ostream that can be written to.
 */
std::ostream & Tracer::trace() {
	return buffer;
}

/**
 * Flushes the data to the file/console.
 */
void Tracer::flush() {
	*traceStream << buffer.str() << "\r\n";
	traceStream->flush();
	buffer.str() = "";
	buffer.clear();
}

/**
 * Switches the Tracer to use the console (cout) for output.
 * This is the default.
 *
 * @return true if the switch could be made
 */
bool Tracer::toConsole() {
	if (traceStream != &std::cout) {
		traceStream = &std::cout;
		fileName = "";
	}

	return true;
}

/**
 * Switches the Tracer to use a file for output.
 *
 * @param filename The name of the file to store the output in
 * @return true if the switch could be made
 */
bool Tracer::toFile(std::string filename) {
	if (fileName != filename) {
		if (filename == "") {
			return toConsole();
		}

		if (fileStream.is_open()) {
			fileStream.close();
		}

		fileStream.clear();

		fileStream.open(filename, std::ios_base::out);
		fileName = filename;
		traceStream = &fileStream;
		return fileStream.good();
	}

	return true;
}

/**
 * Returns true if the tracing is being output to cout
 *
 * @return true if the tracing is being output to cout
 */
bool Tracer::isTracingToConsole() {
	return traceStream == &std::cout;
}


/**
 * Returns true if the tracing is being written to a file
 *
 * @return true if the tracing is being output to a file
 */
bool Tracer::isTracingToFile() {
	return !isTracingToConsole();
}

/**
 * Gets the name if the file the output is being stored in.
 * If isTracingToFile is false, this returns ""
 */
std::string Tracer::getTracingFileName() {
	return fileName;
}


/**
 * Strips a filename of its path
 * @param str filename with path
 * @return filename without path
 */
std::string Tracer::strip_path(const char * str) {
	std::string s = str;
	size_t pos = s.find_last_of("/");
	if (pos == std::string::npos) {
		pos = s.find_last_of("\\");
	}

	if (pos != std::string::npos) {
		s = s.substr(pos + 1);
	}

	return s;
}


} //namespace cpgf

