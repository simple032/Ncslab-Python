#ifndef __NCSLABSFUN_H
#define __NCSLABSFUN_H

void S_FUNCTION_NAME(SimStruct* S)
{
	//S->initializeSize = (MdlInitializeSizeFcn)mdlInitializeSize;
	S->outputs = (MdlOutputsFcn)mdlOutputs;
#if defined(MDL_INITIALIZE_CONDITIONS)
	S->initializeConditions = (MdlInitializeConditionsFcn)mdlInitializeConditions;
#endif // MDL_INITIALIZE_CONDITIONS
	S->start = (MdlStartFcn)mdlStart;
#if defined(MDL_UPDATE)
	S->update = (MdlUpdateFcn)mdlUpdate;
#endif // MDL_UPDATE
#if defined(MDL_DERIVATIVES)
	S->derivatives = (MdlDerivativesFcn)mdlDerivatives;
#endif // MDL_DERIVATIVES
	S->terminate = (MdlTerminateFcn)mdlTerminate;
}
#endif

