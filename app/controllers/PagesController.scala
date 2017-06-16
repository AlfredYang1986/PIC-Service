package controllers


import play.api.mvc._

/**
  * Created by yym on 6/5/17.
  */
class PagesController extends Controller {

//登陆跳转
    def goHome = Action {
        Ok(views.html.home())
    }
    
//登陆
    def login=Action{
        Ok(views.html.login())
    }
//个人中心
    def userInfo=Action{
        Ok(views.html.useInfo())
    }
    def contactus = Action {
        Ok(views.html.contactus())
    }

    def aboutus = Action {
        Ok(views.html.aboutus())
    }
    
    def comingsoon = Action {
        Ok(views.html.comingsonn())
    }
}
