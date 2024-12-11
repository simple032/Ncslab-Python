#ifndef NCSLABSFUN_HPP
#define NCSLABSFUN_HPP

void S_FUNCTION_NAME(SimStruct* S)
{
	S->initializeSizes = (MdlInitializeSizesFcn)mdlInitializeSizes;
	S->initializeSampleTimes = (MdlInitializeSampleTimesFcn)mdlInitializeSampleTimes;
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
	S->outputs = (MdlOutputsFcn)mdlOutputs;
	S->terminate = (MdlTerminateFcn)mdlTerminate;
}
#endif

