#include <iostream>
#include <fstream>

#include "ncslabdefines.hpp"
#include "ncslab.h"
#include "json/json.h"
#include "results.hpp"

extern MODEL* mp;

extern TERMINAL* terminals[];

void writeScope(int cursor, TERMINAL* terminal, Json::Value* pJsonScopes) {
	SCOPE* scope;
	Json::Value jsonScope;
	Json::Value time;
	Json::Value data;
	scope = (SCOPE*)terminal->terminal;


	jsonScope["width"] = scope->width;
	jsonScope["height"] = scope->height;
	jsonScope["name"] = scope->name;

	unsigned int size = scope->timeList.size();
	jsonScope["length"] = size;

	while (scope->timeList.size() > MAX_DATA_POINTS) {
		scope->timeList.pop_front();
		for (int h = 0; h < scope->height; h++) {
			for (int w = 0; w < scope->width; w++) {
				scope->dataList.pop_front();
			}
		}
	}

	int timePos = 0;
	int dataPos = 0;
	while (scope->timeList.empty() == false && scope->dataList.empty() == false) {
		time[timePos] = scope->timeList.front();
		scope->timeList.pop_front();

		for (int h = 0;h < scope->height;h++) {
			for (int w = 0;w < scope->width;w++) {
				data[dataPos] = scope->dataList.front();
				scope->dataList.pop_front();
				dataPos++;
			}
		}

		timePos++;
	}

	jsonScope["time"] = time;
	jsonScope["data"] = data;

	(*pJsonScopes)[cursor] = jsonScope;
}

void NCSLabSaveResult() {
	Json::Value result;
	Json::Value jsonScopes;
	// Json::FastWriter is deprecated, use Json::StreamWriterBuilder instead
	// refer to https://blog.csdn.net/shaosunrise/article/details/84680602
	// Json::FastWriter writer;
	Json::StreamWriterBuilder builder;
	builder.settings_["indentation"] = "";
	std::unique_ptr<Json::StreamWriter> writer(builder.newStreamWriter());
	std::ostringstream os;

	int scopeCursor = 0;

	for (int i = 0;i < mp->terminalNum;i++) {
		TERMINAL* terminal = terminals[i];
		//printf("%p\n",terminal);

		switch (terminal->type) {
		case Scope:
			writeScope(scopeCursor, terminal, &jsonScopes);
			scopeCursor++;
			break;
		}
	}

	result["version"] = "0.1";
	result["scopes"] = jsonScopes;

	// std::string jsonFile = writer.write(result);
	writer->write(result, &os);
	std::string jsonFile = os.str();

	std::ofstream ofs;
	ofs.open("results.json");
	//assert(ofs.is_open());
	ofs << jsonFile;

	//cout<<jsonFile<<endl;

	//cout<<result<<endl;
}
