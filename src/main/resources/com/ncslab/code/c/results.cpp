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
    scope = (SCOPE*)terminal->terminal;

    jsonScope["width"] = scope->width;
    jsonScope["height"] = scope->height;
    jsonScope["name"] = scope->name;
    jsonScope["path"] = scope->path;
    jsonScope["uuid"] = scope->uuid;

    unsigned int size = scope->timeList.size();
    jsonScope["length"] = size;

    // Truncate old data if exceeding MAX_DATA_POINTS
    if (scope->timeList.size() > MAX_DATA_POINTS) {
        size_t overflow = scope->timeList.size() - MAX_DATA_POINTS;
        scope->timeList.erase(scope->timeList.begin(), scope->timeList.begin() + overflow);
        scope->dataList.erase(scope->dataList.begin(),
            scope->dataList.begin() + overflow * scope->height * scope->width);
    }

    // Batch-construct JSON arrays from vectors to avoid per-element assignment overhead
    std::vector<REAL> timeVec(scope->timeList.begin(), scope->timeList.end());
    std::vector<REAL> dataVec(scope->dataList.begin(), scope->dataList.end());
    jsonScope["time"] = std::move(timeVec);
    jsonScope["data"] = std::move(dataVec);

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

	// Truncate old data if exceeding MAX_DATA_POINTS
	if (scope->timeList.size() > MAX_DATA_POINTS) {
		size_t overflow = scope->timeList.size() - MAX_DATA_POINTS;
		scope->timeList.erase(scope->timeList.begin(), scope->timeList.begin() + overflow);
		scope->dataList.erase(scope->dataList.begin(),
			scope->dataList.begin() + overflow * scope->height * scope->width);
	}

	size_t wh = scope->width * scope->height;
	for (size_t t = 0; t < scope->timeList.size(); t++) {
		REAL time = scope->timeList[t];
		fwrite(&(time),sizeof(time),1,fp);

		for(int h=0;h<scope->height;h++){
			for(int w=0;w<scope->width;w++){
				REAL data = scope->dataList[t * wh + h * scope->width + w];
				fwrite(&(data),sizeof(data),1,fp);
			}
		}
	}
}

// ============================================================================
// Scope chunk flush: write current in-memory data to a chunk file and clear
// Called at runtime when timeList exceeds CHUNK_SIZE (100,000)
// ============================================================================
void flushScopeChunk(SCOPE* scope) {
    if (scope->timeList.empty()) {
        return;
    }

    json scopeJson;
    scopeJson["uuid"] = scope->uuid;
    scopeJson["name"] = scope->name;
    scopeJson["path"] = scope->path;
    scopeJson["width"] = scope->width;
    scopeJson["height"] = scope->height;
    scopeJson["version"] = "0.2";
    scopeJson["chunkIndex"] = scope->chunkCount;

    // Batch-construct JSON arrays from vectors to avoid per-element push_back overhead
    std::vector<REAL> timeVec(scope->timeList.begin(), scope->timeList.end());
    std::vector<REAL> dataVec(scope->dataList.begin(), scope->dataList.end());
    scopeJson["time"] = std::move(timeVec);
    scopeJson["data"] = std::move(dataVec);

    std::string filename = std::string("scope_") + scope->uuid + "_chunk" + std::to_string(scope->chunkCount) + ".json";
    std::ofstream scopeFile(filename);
    scopeFile << scopeJson << std::endl;

    // Clear vectors but retain allocated capacity for reuse
    scope->timeList.clear();
    scope->dataList.clear();

    scope->chunkCount++;
}

void NCSLabSaveResultBin(){
	json scopesMeta = json::array();
	int scopeCursor=0;

	writeSavingInformation(0,mp->terminalNum);
	for(int i=0;i<mp->terminalNum;i++){
		TERMINAL *terminal=terminals[i];
		if(terminal->type==Scope){
			SCOPE *scope=(SCOPE *)terminal->terminal;

			// 1. Build individual scope JSON with remaining in-memory data
			json scopeJson;
			scopeJson["uuid"] = scope->uuid;
			scopeJson["name"] = scope->name;
			scopeJson["path"] = scope->path;
			scopeJson["width"] = scope->width;
			scopeJson["height"] = scope->height;
			scopeJson["version"] = "0.2";
			scopeJson["chunkIndex"] = scope->chunkCount;

			// Batch-construct JSON arrays from vectors to avoid per-element push_back overhead
			std::vector<REAL> timeVec(scope->timeList.begin(), scope->timeList.end());
			std::vector<REAL> dataVec(scope->dataList.begin(), scope->dataList.end());
			scopeJson["time"] = std::move(timeVec);
			scopeJson["data"] = std::move(dataVec);

			// Write final scope file (always scope_<uuid>.json for the last chunk)
			std::string filename = std::string("scope_") + scope->uuid + ".json";
			std::ofstream scopeFile(filename);
			scopeFile << scopeJson << std::endl;

			// 2. Build metadata entry with chunks list
			json meta;
			meta["uuid"] = scope->uuid;
			meta["name"] = scope->name;
			meta["path"] = scope->path;
			meta["width"] = scope->width;
			meta["height"] = scope->height;

			json chunks = json::array();
			for (int c = 0; c < scope->chunkCount; c++) {
				chunks.push_back(std::string("scope_") + scope->uuid + "_chunk" + std::to_string(c) + ".json");
			}
			chunks.push_back(filename); // final chunk
			meta["chunks"] = chunks;

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
