package com.been.catego.interceptor;

import com.been.catego.dto.PrincipalDetails;
import com.been.catego.dto.response.FolderInfoWithChannelResponse;
import com.been.catego.service.FolderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@RequiredArgsConstructor
public class FolderListInterceptor implements HandlerInterceptor {

    private final FolderService folderService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
        if (modelAndView != null) {
            PrincipalDetails principalDetails = (PrincipalDetails) SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getPrincipal();

            List<FolderInfoWithChannelResponse> folders =
                    folderService.getAllFolderInfoWithChannelsByUserId(principalDetails.getId());
            modelAndView.addObject("folders", folders);
        }
    }
}
