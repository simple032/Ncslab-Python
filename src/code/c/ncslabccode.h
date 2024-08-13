#ifndef NCSLABCCODE
#define NCSLABCCODE

#include <float.h>
#include <stdio.h>
#include <stdbool.h>
#include <stdlib.h>
#include <string.h>

#include<errno.h>

#include<signal.h>
#include<time.h>

#if defined(_WIN32) || defined(_WIN64)
#include "winsock2.h"
#include <windows.h>

#elif defined(__linux__)
#include<sys/types.h>
#include<sys/socket.h>
#include<netinet/in.h>
#include <sys/time.h>
#include <unistd.h>
#include <pthread.h>
#include "arpa/inet.h"
#include "fcntl.h"

#ifdef RASP_PI
#include "ADS1256.h"
#include "DAC8532.h"
#include "ncs_serialport.h"
#include <wiringPi.h>
#include <termios.h>
#endif // RASP_PI

#include <linux/input.h>
#endif //

#include <vector>
#include <iostream>
// #include <octave/oct.h>




enum DATA_TYPE {SINGLE,MATRIX};

class Matrix {
private:
    std::vector<std::vector<double>> data;
    size_t _rows, _cols;

public:
    // Constructor to initialize matrix with given dimensions and initial value
    Matrix(size_t rows, size_t cols, double initial = 0.0)
        : _rows(rows), _cols(cols), data(rows, std::vector<double>(cols, initial)) {}

    // Overload the () operator to provide element access
    double& operator()(size_t row, size_t col) {
        if (row >= _rows || col >= _cols) {
            throw std::out_of_range("Index out of bounds");
        }
        return data[row][col];
    }

    // Const overload for () operator to provide read-only element access
    const double& operator()(size_t row, size_t col) const {
        if (row >= _rows || col >= _cols) {
            throw std::out_of_range("Index out of bounds");
        }
        return data[row][col];
    }

    // Get the number of rows
    size_t rows() const {
        return _rows;
    }

    // Get the number of columns
    size_t cols() const {
        return _cols;
    }

    // Print the matrix
    void print() const {
        for (size_t i = 0; i < _rows; ++i) {
            for (size_t j = 0; j < _rows; ++j) {
                std::cout << data[i][j] << " ";
            }
            std::cout << std::endl;
        }
    }

	// Overload the * operator for scalar multiplication
    Matrix operator*(double scalar) const {
        Matrix result(_rows, _cols);
        for (size_t i = 0; i < _rows; ++i) {
            for (size_t j = 0; j < _cols; ++j) {
                result(i, j) = data[i][j] * scalar;
            }
        }
        return result;
    }

    // Overload the * operator for scalar multiplication with scalar on the left
    friend Matrix operator*(double scalar, const Matrix& mat) {
        return mat * scalar;  // Reuse the member operator*
    }

    // Overload the += operator for matrix addition
    Matrix& operator+=(const Matrix& other) {
        if (_rows != other._rows || _cols != other._cols) {
            throw std::invalid_argument("Matrix dimensions must match for addition");
        }
        for (size_t i = 0; i < _rows; ++i) {
            for (size_t j = 0; j < _cols; ++j) {
                data[i][j] += other(i, j);
            }
        }
        return *this;
    }

    // Overload the + operator for matrix addition
    Matrix operator+(const Matrix& other) const {
        Matrix result = *this;
        result += other;
        return result;
    }
};

#ifdef __cplusplus
extern "C" {
#endif

#define REAL double
#define real_T REAL
#define uint_T unsigned int
#define int_T int
#define char_T char

typedef struct {
	char *name;
	int width;
	int height;
	DATA_TYPE type;
	void *vp;
}INPUT_PORT;

typedef struct {
	char *name;
	int width;
	int height;
	DATA_TYPE type;
	void *vp;
}OUTPUT_PORT;

typedef struct {
	char *name;
	char *path;
	int width;
	int height;
	DATA_TYPE type;
	void *vp;
}PARAMETER;

typedef struct {
	char *name;
	int width;
	int height;
	DATA_TYPE type;
	void *vp;
	void *dvp;
}STATE;

typedef struct {
	char *name;
	char *path;
	int width;
	int height;
	DATA_TYPE type;
	void *vp;
}SIGNAL;


typedef void(*MdlUpdateFcn)(void* S);
typedef void(*MdlOutputsFcn)(void* S, int tid);
typedef void(*MdlStartFcn)(void* S);
typedef void(*MdlDerivativesFcn)(void* S);
typedef void(*MdlTerminateFcn)(void* S);

typedef struct {
	char *type;
	char *name;
	int inputPortNum;
	int outputPortNum;
	int parameterNum;
	int stateNum;
	int signalNum;
	
	INPUT_PORT **inputPorts;
	OUTPUT_PORT **outputPorts;
	PARAMETER **parameters;
	STATE **states;
	SIGNAL **signals;

}BLOCK;

typedef struct SimStruct_tag{
	MdlUpdateFcn update;
	MdlOutputsFcn outputs;
	MdlStartFcn start;
	MdlDerivativesFcn derivatives;
	MdlTerminateFcn terminate;
	BLOCK* parentBlock;
}SimStruct;

typedef struct {
	char *name;
	int blockNum;
	REAL stepSize;
	REAL startTime;
	REAL stopTime;
	REAL time;
	REAL offset;
	
	int signalNum;
	int parameterNum;
	int stateNum;
	SIGNAL **signals;
	PARAMETER **parameters;
	STATE **states;
	
	BLOCK **blocks;
	int majorStep;
	struct timeval tv;
	
	int terminalNum;
	
}MODEL;

enum TERMINALTYPE{Scope};

typedef struct{
	enum TERMINALTYPE type;
	void *terminal;
}TERMINAL;

typedef struct{
	char *name;
	int maxDataLength;
	int width;
	int height;
	int cursor;
	REAL *buffer;
	REAL *timeBuffer;
	
	int isFull;
}SCOPE;

void NCSLabInit();
void NCSLabOneStep();
void NCSLabOutput();
void NCSLabDerivative();
void NCSLabUpdate();
void NCSLabTerminate();
void storeState(int);
void restoreState(int);
void storeDerivative(int);
REAL calculateStateDif(int,int);
void caculateDerivative(double *,int);
MODEL * NCSLabGetModelP();

void ncslabLoop();

void NCSLabSaveResult();

unsigned char calcSum(unsigned char bytes[]);
#ifdef __cplusplus
}
#endif
#endif

