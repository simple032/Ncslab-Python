# RepeatingSequence模块基本教程

在此介绍RepeatingSequence基本操作。

## Output
**Constant用于生成周期锯齿波**

## Parameters

### Time Value
格式为1xn的数组,表示锯齿波任意周期的起止时间
如[0,2]表示信号以2s的周期重复.
[0,2,4]表示信号以4s周期重复,且周期内分两段0-2和2-4.
### Output Value
格式为1xn的数组,表示锯齿波任意周期的信号起止值
如[0,2]表示信号的起始值0,终止值2.
[0,2,4]表示信号第1段起始值0,终止值2；第2段起始值2,终止值4.


