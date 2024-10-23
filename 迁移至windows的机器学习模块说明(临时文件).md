# 将机器学习的开发环境部署到windows

-M2PLink

## 环境部署

* MinGW64配置

前往官网下载和配置MinGW64，并将`path/to/MinGW64/bin`添加到您的环境变量路径中。

* CMake环境

请检查你的cmake版本：

```bash
cmake --version
```

如果版本号低于3.30，或者不具有CMake，请卸载并重新安装新版本。

> 卸载已有CMake：在“控制面板“中选择cmake，右键卸载；同时删除你原来环境变量中配置的cmake。

* vcpkg

首先，进入[vcpkg官网](https://learn.microsoft.com/zh-cn/vcpkg/get_started/get-started-vscode?pivots=shell-powershell)查看相关教程。将这个仓库克隆到本地：

```bash
git clone https://github.com/microsoft/vcpkg.git
```

然后进入该仓库，运行脚本进行安装：

```bash
.\bootstrap-vcpkg.bat
```

然后，将`path\to\vcpkg`添加到环境变量路径中。

* Eigen3和

依次完成上述步骤后，运行下列指令进行安装：

```bash
vcpkg install eigen3
vcpkg install nlohmann-json
```

使用该命令可以安装最新版的eigen。本次采用的eigen版本为3.4.0正式发布版。

## 使用机器学习开发测试工具箱

首先，将`M2PLab\server\openresty\conf\nginx.conf`替换为如下内容：

```
events {
	worker_connections  1024;
}


http {
	include   mime.types;
    	default_type  application/octet-stream;

    	keepalive_timeout  65;

    	upstream myfastcgi {
	       server 127.0.0.1:9000 weight=1;
	       server 127.0.0.1:9001 weight=1;
	       server 127.0.0.1:9002 weight=1;
	       server 127.0.0.1:9003 weight=1;
	}


	server {
		# error_log  logs/error.log debug;
		# rewrite_log on;
		listen 80;
		index index.html;
		server_name localhost;

		set_by_lua $M2PLAB_ROOT 'return os.getenv("M2PLAB_ROOT")';

		set $frontend_root $M2PLAB_ROOT/frontend/dist;
		set $backend_root $M2PLAB_ROOT/backend/public;

		root $frontend_root;

		location = / {
			try_files $uri $uri/ /index.html;
		}
		
		
		location ^~ /experiment/ {
			try_files $uri $uri/ /experiment.html;
		}

		location ~* \/(admin|lab|login|regist|forgotpwd|resetpwd) {
			try_files $uri $uri/ /app.html;
		}

		location ^~ /three/ {
			root ../../frontend/;
		}

		location ^~ /3DModels/ {
			root ../../frontend/src/;
		}

		location ^~ /Doc/ {
			root ../../frontend/src/;
		}

		location ^~ /joint/ {
		    root ../../frontend/;
		}	


		location ^~ /locales/ {
			root ../../frontend/src/;
		}

		# location /matlab/compile {
		# 	proxy_pass http://192.168.46.128:8081/api/task/cmp;
		# }

		# location /matlab/simulate {
		# 	proxy_pass http://192.168.46.128:8081/api/task/sim;
		# }

		location  /result {
			root E:\WHU\matlab_server\matlab_ncs;
		}

		# location /matlab/function {
		# 	proxy_pass http://192.168.46.128:8081/api/task/function;
		# }

		# location /matlab/bode {
		# 	proxy_pass http://192.168.46.128:8081/api/simulate/bode;
		# }

		# location /matlab/nyquist {
		# 	proxy_pass http://192.168.46.128:8081/api/simulate/nyquist;
		# }

		# location /matlab/rlocus {
		# 	proxy_pass http://192.168.46.128:8081/api/simulate/rlocus;
		# }			

		# location  /matlab/compilePi {
		# 	proxy_pass http://192.168.46.102:8081/api/task/cmp;
		# }

		# location  /matlab/simulatePi {
		# 	proxy_pass http://192.168.46.102:8081/api/task/sim;
		# }
		

		# location ~* \/(m) {
		#	try_files $uri $uri/ /mobile.html;
		# }

		#backend


		location ^~ /api/ {
			root $backend_root;
		    	try_files $uri $uri/ /index.php?$query_string;
		}

		location ~ \.php$ {
		    	fastcgi_pass   myfastcgi;
		    	fastcgi_index  index.php;
		    	fastcgi_param  SCRIPT_FILENAME  $backend_root$fastcgi_script_name;
		    	include    fastcgi_params;
		    	break;
		}

		# service
		location ^~ /rtlab/  {
      			proxy_pass http://localhost:8070/rtlab/;
			proxy_http_version 1.1;
    			proxy_set_header Upgrade $http_upgrade;
    			proxy_set_header Connection "Upgrade";
    	}

		location ^~ /matlab/  {
      			proxy_pass http://localhost:8070/NCSLabLink/;
			proxy_http_version 1.1;
    			proxy_set_header Upgrade $http_upgrade;
    			proxy_set_header Connection "Upgrade";
    	}

		location /octave {
			proxy_pass http://localhost:8070/NCSLabLink/octave;
		}

		location /mfcalc {
			proxy_pass http://localhost:8070/NCSLabLink/mfcalc;
		}
		
		location /CCode/{
			root ../../data/;
		}
	}
}
```

然后，文本编辑`M2PLab\server\start.bat`对第三十一行进行注释（前面加上百分号）：

```
%set PATH=%DIRNAME%\cruntime\bin;%PATH%
```

现在，你可以通过以下手段进行前后端运行了：

* 后端：

在你的IDEA中，选择`build`->`build artifacts...`->`build all artifacts`->`build`（第一次）/`rebuild`（非第一次）。

然后，前往该工程文件的target文件夹，找到`ncslablink-xxxx`文件夹和`ncslablink-xxxx.war`文件，将它们拷贝到`M2PLab\server\tomcat\webapps\`文件夹，替换该目录中原有的`NCSLabLink`和`NCSLabLink.war`文件。注意，替换后，请将文件名分别命名为`NCSLabLink`和`NCSLabLink.war`。

* 前端：

进入你的前端工程目录，打开命令行，输入：

```bash
npm run build
```

然后，将该工程根目录中的`build`文件夹内的内容全部拷贝，并替换掉`M2PLab\frontend\dist\`中的所有内容。