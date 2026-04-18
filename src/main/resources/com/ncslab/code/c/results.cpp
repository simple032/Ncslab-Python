#include <fstream>
#include "nlohmann/json.hpp"

#include "ncslabdefines.hpp"
#include "ncslabccode.hpp"
#include "ncslab.hpp"
#include "results.hpp"

using json = nlohmann::json;

extern MODEL* mp;
extern TERMINAL* terminals[];

void writeScope(int cursor, TERMINAL* terminal, json* pJsonScopes) {
    SCOPE* scope;
    json jsonScope;
    json time;
    json data;
    scope = (SCOPE*)terminal->terminal;

    jsonScope["width"] = scope->width;
    jsonScope["height"] = scope->height;
    jsonScope["name"] = scope->name;
    jsonScope["path"] = scope->path;
    jsonScope["uuid"] = scope->uuid;

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
        timePos++;

        for (int h = 0;h < scope->height;h++) {
            for (int w = 0;w < scope->width;w++) {
                data[dataPos] = scope->dataList.front();
                scope->dataList.pop_front();
                dataPos++;
            }
        }
    }

    jsonScope["time"] = time;
    jsonScope["data"] = data;

    (*pJsonScopes)[cursor] = jsonScope;
}

void NCSLabSaveResult() {
    json result;
    json jsonScopes;

    int scopeCursor = 0;

    for (int i = 0; i < mp->terminalNum; i++) {
        TERMINAL* terminal = terminals[i];

        switch (terminal->type) {
            case Scope:
                writeScope(scopeCursor, terminal, &jsonScopes);
                scopeCursor++;
                break;
        }
    }

    result["version"] = "0.1";
    result["scopes"] = jsonScopes;

    // Write the JSON to a file
    std::ofstream file("results.json");
    // if you want to write json file with indentation
    // using the setw() function
    // file << std::setw(4) << result << std::endl;
    file << result << std::endl;
}

typedef struct {
	int nameLength;
	int size;
	int maxDataLength;
	int width;
	int height;
	int isFull;

}SCOPE_STRUCT;

void writeScopeBin(int cursor,TERMINAL *terminal,FILE *fp){
	SCOPE *scope;
	SCOPE_STRUCT scopeStruct;
	scope=(SCOPE *)terminal->terminal;
	scopeStruct.height=scope->height;
	scopeStruct.width=scope->width;
	scopeStruct.isFull=scope->isFull;
	scopeStruct.size=(scope->timeList.size()<MAX_DATA_POINTS)?scope->timeList.size():MAX_DATA_POINTS;
	scopeStruct.maxDataLength=scope->maxDataLength;
	scopeStruct.nameLength=strlen(scope->name);
	//strcpy(scopeStruct.name,scope->name);

	fwrite(&(scopeStruct),sizeof(scopeStruct),1,fp);
	fwrite(scope->name,scopeStruct.nameLength,1,fp);
	int uuidSize=strlen(scope->uuid);
	fwrite(&uuidSize,sizeof(uuidSize),1,fp);
	fwrite(scope->uuid,uuidSize,1,fp);

	while(scope->timeList.size()>MAX_DATA_POINTS){
		scope->timeList.pop_front();
		for(int h=0;h<scope->height;h++){
			for(int w=0;w<scope->width;w++){
				scope->dataList.pop_front();
			}
		}
	}
	int timePos=0;
	int dataPos=0;
	while(scope->timeList.empty()==false&&scope->dataList.empty()==false){
		REAL time=scope->timeList.front();
		scope->timeList.pop_front();
		fwrite(&(time),sizeof(time),1,fp);

		for(int h=0;h<scope->height;h++){
			for(int w=0;w<scope->width;w++){
				REAL data=scope->dataList.front();
				scope->dataList.pop_front();
				fwrite(&(data),sizeof(data),1,fp);
				dataPos++;
			}
		}

		timePos++;
	}
}

void NCSLabSaveResultBin(){
	json scopesMeta = json::array();
	int scopeCursor=0;

	writeSavingInformation(0,mp->terminalNum);
	for(int i=0;i<mp->terminalNum;i++){
		TERMINAL *terminal=terminals[i];
		if(terminal->type==Scope){
			SCOPE *scope=(SCOPE *)terminal->terminal;

			// 1. Build individual scope JSON with full data
			json scopeJson;
			scopeJson["uuid"] = scope->uuid;
			scopeJson["name"] = scope->name;
			scopeJson["path"] = scope->path;
			scopeJson["width"] = scope->width;
			scopeJson["height"] = scope->height;
			scopeJson["version"] = "0.2";

			// Trim data to MAX_DATA_POINTS
			while(scope->timeList.size()>MAX_DATA_POINTS){
				scope->timeList.pop_front();
				for(int h=0;h<scope->height;h++){
					for(int w=0;w<scope->width;w++){
						scope->dataList.pop_front();
					}
				}
			}

			json timeArray = json::array();
			json dataArray = json::array();
			while(!scope->timeList.empty() && !scope->dataList.empty()){
				timeArray.push_back(scope->timeList.front());
				scope->timeList.pop_front();
				for(int h=0;h<scope->height;h++){
					for(int w=0;w<scope->width;w++){
						dataArray.push_back(scope->dataList.front());
						scope->dataList.pop_front();
					}
				}
			}
			scopeJson["time"] = timeArray;
			scopeJson["data"] = dataArray;

			// Write individual scope file
			std::string filename = std::string("scope_") + scope->uuid + ".json";
			std::ofstream scopeFile(filename);
			scopeFile << scopeJson << std::endl;

			// 2. Build metadata entry
			json meta;
			meta["uuid"] = scope->uuid;
			meta["name"] = scope->name;
			meta["path"] = scope->path;
			meta["width"] = scope->width;
			meta["height"] = scope->height;
			scopesMeta.push_back(meta);

			scopeCursor++;
		}
		writeSavingInformation(i+1,mp->terminalNum);
	}

	// 3. Write metadata results.json
	json result;
	result["version"] = "0.2";
	result["scopes"] = scopesMeta;
	std::ofstream metaFile("results.json");
	metaFile << result << std::endl;
}
