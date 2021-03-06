package xin.nbjzj.rehab.core.action;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import reactor.core.publisher.Mono;
import xin.nbjzj.rehab.core.entity.User;
import xin.nbjzj.rehab.core.entity.request.LoginReq;
import xin.nbjzj.rehab.core.entity.response.UserResp;
import xin.nbjzj.rehab.core.service.UserRepository;

@Api(tags = "登录相关接口")
@RestController
@RequestMapping("/login")
public class LoginController {
	private UserRepository userRepository;
	public LoginController(UserRepository userRepository) {
		super();
		this.userRepository = userRepository;
	}
	@ApiOperation(value = "登录接口" ,  notes="上传学工号和密码进行登录")
	@ApiResponses({@ApiResponse(code = 200, message = "操作成功",response = UserResp.class),
        @ApiResponse(code = 500, message = "服务器内部异常"),
        @ApiResponse(code = 404, message = "不存在的账号"),
        @ApiResponse(code = 401, message = "密码错误")})
	@PostMapping("/dologin")
	public Mono<ResponseEntity<UserResp>> doLogin(@ApiParam(value="以json格式放入Request Body中",required=true) @Valid @RequestBody LoginReq loginReq,HttpSession session){
		User youruser = new User(loginReq);
		User myuser = userRepository.findByPhone(youruser.getPhone());
		System.out.println(myuser);
		if(myuser==null) {
			//不存在的手机号
			return Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND));
		}
		if(!youruser.getPassword().equals(myuser.getPassword())) {
			//密码错误
			return Mono.just(new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
		}
		
		
		UserResp userResp= new UserResp(myuser);
		session.setAttribute("phone", userResp.getPhone());
		session.setAttribute("identity", userResp.getIdentity());
		session.setAttribute("instution", userResp.getInstitution());
		session.setAttribute("user_id", userResp.getUser_id());
		session.setAttribute("user_name", userResp.getUser_name());
		session.setAttribute("public_key", myuser.getPublicKey());
		session.setAttribute("private_key", myuser.getPrivateKey());
		return Mono.just(new ResponseEntity<UserResp>(userResp,HttpStatus.OK));
	}
	
	@ApiOperation(value = "Session信息获取接口" ,  notes="获取登录状态")
	@ApiResponses({@ApiResponse(code = 200, message = "操作成功",response = UserResp.class),
        @ApiResponse(code = 500, message = "服务器内部异常"),
        @ApiResponse(code = 400, message = "客户端请求的语法错误,服务器无法理解"),
        @ApiResponse(code = 405, message = "权限不足")})
	@PostMapping("/session")
	public Mono<ResponseEntity<UserResp>> getSession(HttpSession session){
		UserResp userResp= new UserResp();
		if(session.getAttribute("identity")==null) {
			return Mono.just(new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED));
		}
		
		userResp.setIdentity(session.getAttribute("identity").toString());
		userResp.setUser_id((Long)session.getAttribute("user_id"));
		userResp.setUser_name(session.getAttribute("user_name").toString());
		userResp.setInstitution(session.getAttribute("instution").toString());
		userResp.setPhone(session.getAttribute("phone").toString());
		return Mono.just(new ResponseEntity<UserResp>(userResp,HttpStatus.OK));
	}
	
	
	
	@ApiOperation(value = "登出接口" ,  notes="清除登录状态")
	@ApiResponses({@ApiResponse(code = 200, message = "操作成功",response = UserResp.class),
        @ApiResponse(code = 500, message = "服务器内部异常"),
        @ApiResponse(code = 400, message = "客户端请求的语法错误,服务器无法理解"),
        @ApiResponse(code = 405, message = "权限不足")})
	@PostMapping("/logout")
	public Mono<ResponseEntity<Object>> doLogout(HttpSession session){
		
		if(session.getAttribute("identity")==null) {
			return Mono.just(new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED));
		}
		session.setAttribute("identity", null);
		session.setAttribute("user_id", null);
		session.setAttribute("user_name", null);
		session.setAttribute("phone", null);
		return Mono.just(new ResponseEntity<>(HttpStatus.OK));
	}
	
	
	
	
}
