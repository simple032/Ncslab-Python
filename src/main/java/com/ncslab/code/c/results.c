#include "ncslabccode.h"
#include "ServerThread.h"
#include "ncslab.h"
#include "jsoncpp/json/json.h"
#include <iostream>
#include <fstream>

using namespace std;

extern MODEL* mp;

extern TERMINAL *terminals[];

void writeScope(int cursor,TERMINAL *terminal,Json::Value *pJsonScopes){
	SCOPE *scope;
	Json::Value jsonScope;
	Json::Value time;
	Json::Value data;
	scope=(SCOPE *)terminal->terminal;

	
	jsonScope["width"]=scope->width;
	jsonScope["height"]=scope->height;
	jsonScope["name"]=scope->name;

	unsigned int size=scope->timeList.size();
	jsonScope["length"]=size;
	
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
		time[timePos]=scope->timeList.front();
		scope->timeList.pop_front();

		for(int h=0;h<scope->height;h++){
			for(int w=0;w<scope->width;w++){
				data[dataPos]=scope->dataList.front();
				scope->dataList.pop_front();
				dataPos++;
			}
		}

		timePos++;
	}

	jsonScope["time"]=time;
	jsonScope["data"]=data;

	(*pJsonScopes)[cursor]=jsonScope;
}

void NCSLabSaveResult(){
	Json::Value result;
	Json::Value jsonScopes;
	Json::FastWriter writer;
	
	int scopeCursor=0;

	for(int i=0;i<mp->terminalNum;i++){
		TERMINAL *terminal=terminals[i];
		//printf("%p\n",terminal);
		
		switch(terminal->type){
		case Scope:
			writeScope(scopeCursor,terminal,&jsonScopes);
			scopeCursor++;
			break;
		}
	}
	
	result["version"]="0.1";
	result["scopes"]=jsonScopes;
	
	string jsonFile=writer.write(result);

	ofstream ofs;
	ofs.open("results.json");
	//assert(ofs.is_open());
	ofs<<jsonFile;
	
	//cout<<jsonFile<<endl;

	//cout<<result<<endl;
}
