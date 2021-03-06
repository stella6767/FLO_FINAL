package com.cos.flopjt.web;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cos.flopjt.config.auth.PrincipalDetails;
import com.cos.flopjt.domain.playlist.PlaySong;
import com.cos.flopjt.domain.user.User;
import com.cos.flopjt.service.PlaylistService;
import com.cos.flopjt.service.UserService;
import com.cos.flopjt.web.dto.CMRespDto;
import com.cos.flopjt.web.dto.playlist.PlaylistSaveReqDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class PlaylistController {

	private final UserService userService;
	private final PlaylistService playlistService;
	
	@GetMapping("/list/{id}")
	public String list(@PathVariable int id, Model model) {
		User listEntity = userService.노래보기(id);
		
		List<PlaySong> userPl = userService.유저플레이리스트찾기(id); 
		
		
		//select $ from playsong where userId=? and =?
		model.addAttribute("user", listEntity);
		model.addAttribute("userPl", userPl);
		
		
		return "playlist/playlistForm";
	}
	
	@PostMapping("/listAdd")
	public @ResponseBody CMRespDto<?> save(@RequestBody PlaylistSaveReqDto playlistSaveReqDto, @AuthenticationPrincipal PrincipalDetails principalDetails) {
		PlaySong playlistEntity = playlistService.리스트추가(playlistSaveReqDto, principalDetails.getUser());
		
		if (playlistEntity == null) {
			return new CMRespDto<>(-1, "리스트 추가 실패", null);
		}else {
			return new CMRespDto<>(1, "리스트 추가 성공", playlistEntity);
		}
	}
}
