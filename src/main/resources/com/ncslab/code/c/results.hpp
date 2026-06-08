#ifndef RESULTS_HPP
#define RESULTS_HPP

struct SCOPE;

void NCSLabSaveResult();
void NCSLabSaveResultBin();
void flushScopeChunk(SCOPE* scope);
int ncsScopeShouldLog(SCOPE* scope);

#endif
