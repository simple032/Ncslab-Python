#ifndef MAINCCODE_HPP
#define MAINCCODE_HPP

#include "ncslabdefines.hpp"

extern int sample_i;
extern double sample_time[];

void NCSLabInit();
void NCSLabOneStep();
void NCSLabOutput();
void NCSLabDerivative();
void NCSLabUpdate();
void NCSLabDiscreteUpdate();
void NCSLabTerminate();
void NCSLabSinkOutput();
void NCSLabFinalize();
MODEL *  NCSLabGetModelP();

#endif