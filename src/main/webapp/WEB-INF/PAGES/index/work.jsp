<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/jquery.min.js"></script>
<head>
    <meta charset="UTF-8">
    <title>高苏杨专用查班系统</title>
</head>
<script type="text/javascript">


    function showFlyOther(index,fltNum,portA,portB,actStrDtTmGmt,seriesNum){
        $("#div_other"+index).toggle();
        if(!$("#div_other"+index).is(":hidden") && $("#crewInfoDiv"+index).html().length==0){
            $.ajax({
                type:"POST",
                dataType:"text",
                data:{actStrDtTmGmt:actStrDtTmGmt,fltNum:fltNum,portA:portA,portB:portB,seriesNum:seriesNum},
                url:"/newportal/cabin/scheduleplan-SchedulePlan-showCrewInfo.do",
                success:function(data){
                    $("#crewInfoDiv"+index).html(data);
                    refreshIFrameHeight();
                },
                error:function(XMLHttpRequest, textStatus, errorThrown){
                    alert(errorThrown);
                    refreshIFrameHeight();
                }
            });
        }
        refreshIFrameHeight();
    }

</script>
<body  >
<form id="indexForm" action="/login">
    ${workResult}
    <br/><br/>
        ${jizuResult}
</form>
</body>



</html>
