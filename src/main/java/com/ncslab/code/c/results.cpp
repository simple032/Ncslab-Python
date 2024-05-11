#include <fstream>
#include "nlohmann/json.hpp"

#include "ncslabdefines.hpp"
#include "ncslab.h"
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
