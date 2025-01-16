package demo

import spock.lang.Specification

class Component{
    Component(suf = "suffix"){
        this.suf=suf
    }
    private final String suf
    def suf(){
        return suf.toLowerCase()
    }
    def that(msg=""){}
    def other(){}
}

class Service{
    Service(comp = new Component()){
        this.comp = comp
    }
    Component comp
    def call(String pre){
        comp.that(pre)
        return pre+"."+comp.suf()
    }
}
class DemoTest extends Specification{

    def "0.test demo"(){
        given:
        var a = "ok"
        when:
        println(a)
        then:
        true
    }
    Component comp = Mock()
    var target = new Service(comp)

    def "1 demo test Service.call"(){
        given:
        var prefix = "pre"
        when:
        var res = target.call(prefix)
        println(res)
        then:
        //[times] * [component call] >> [mocked return value]
        1 * comp.suf() >> "SUF" //mock action of component
        1 * comp.that{it!="" && it=="pre"} >> void
        0 * _ // other call 0 times
        res == "pre.SUF" // equals
    }
}
