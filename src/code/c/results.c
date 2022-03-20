#include"ncslabccode.h"
#include"ServerThread.h"
#include"ncslab.h"
#include "json/json.h"
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

	if(scope->isFull){
		int pos=scope->cursor;
		for(int i=0;i<scope->maxDataLength;i++){
			pos%=scope->maxDataLength;
			time[i]=scope->timeBuffer[pos];

			int dataPos=pos*scope->height*scope->width;
			int dataCursorPos=i*scope->height*scope->width;
			for(int h=0;h<scope->height;h++){
				for(int w=0;w<scope->width;w++){
					data[dataCursorPos]=scope->buffer[dataPos];
					dataPos++;
					dataCursorPos++;
				}
			}

			pos++;
		}

		jsonScope["length"]=scope->maxDataLength;
	}
	else{
		for(int i=0;i<scope->cursor;i++){
			time[i]=scope->timeBuffer[i];
			int dataCursorPos=i*scope->height*scope->width;
			for(int h=0;h<scope->height;h++){
				for(int w=0;w<scope->width;w++){
					data[dataCursorPos]=scope->buffer[dataCursorPos];
					dataCursorPos++;
				}
			}
		}
		jsonScope["length"]=scope->cursor;
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
