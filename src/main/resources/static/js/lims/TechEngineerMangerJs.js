layui.use(['form', 'table'], function () {
    var $ = layui.jquery,
        form = layui.form,
        table = layui.table;

    table.render({
        elem: '#currentTableId',
        url: '/TechEngineerTaskByUser',
        method:'post',
        toolbar: '#toolbarDemo',
        request: {
            pageName: 'page' //页码的参数名称，默认：page
            , limitName: 'limit' //每页数据量的参数名，默认：limit
        },
        response: {
            statusName: 'code' //数据状态的字段名称，默认：code
            , statusCode: 0 //成功的状态码，默认：0
            , countName: 'totals' //数据总数的字段名称，默认：count
            , dataName: 'list' //数据列表的字段名称，默认：data
        },
        defaultToolbar: ['filter', 'exports', 'print', {
            title: '提示',
            layEvent: 'LAYTABLE_TIPS',
            icon: 'layui-icon-tips'
        }],
        cols: [
            [
                {field: 'project', width: 165, title: '任务单号', sort: true},
                {field: 'taskId', width: 200, title: '任务名称', sort: true},
                {field: 'taskReportedName', width: 100, title: '测试人员', sort: true},
                {field: 'analysis', width: 100, title: '提交时间', sort: true},
                {field: 'testNumber', width: 100, title: '所属工程师', sort: true},
                {field: 'planTestTime', width: 100, title: '报告语言', sort: true},
                {field: 'planTestTime', width: 100, title: '驳回信息', sort: true}
            ]
        ],
        limit: 10,
        limits: [10, 20, 30, 40, 50, 100],
        page: true,
        skin: 'line'

    });

    // 监听搜索操作
    form.on('submit(data-search-btn)', function (data) {
        var result = JSON.stringify(data.field);
        /*layer.alert(result, {
            title: '提示'
        });*/

        //执行搜索重载
        table.reload('currentTableId', {
            page: {
                curr: 1
            }
            , where: {
                keyword: result
            }
        }, 'list');

        return false;
    });

    /**
     * toolbar监听事件
     */
    table.on('toolbar(currentTableFilter)', function (obj) {
        var checkStatus = table.checkStatus('currentTableId')
            , data = checkStatus.data;

        if (obj.event === 'sample') {  // 监听样品管理员跳转
            layer.confirm('确定将任务转换至样品接收状态吗？', function (index) {
                //layer.alert(JSON.stringify(data));
                $.ajax({
                    type:"post",
                    url:"/lims/updateToSign",
                    data:JSON.stringify(data),
                    contentType:"application/json;charset=utf-8",
                    dataType:"json",
                    success:function(res){
                        console.log(res);
                        layer.alert('跳转成功');
                    }
                });
            });
        };
        if (obj.event === 'plan') {  // 监听排程跳转
            layer.confirm('确定将任务转换至计划排程状态吗？', function (index) {
                $.ajax({
                    type:"post",
                    url:"/lims/updateToPlan",
                    data:JSON.stringify(data),
                    contentType:"application/json;charset=utf-8",
                    dataType:"json",
                    success:function(res){
                        console.log(res);
                        layer.alert('跳转成功');
                    }
                });
            });
        };
        if (obj.event === 'distribution') {  // 监听任务分配操作
            layer.confirm('确定将任务转换至任务分配状态吗？', function (index) {
                $.ajax({
                    type:"post",
                    url:"/lims/updateToBistribution",
                    data:JSON.stringify(data),
                    contentType:"application/json;charset=utf-8",
                    dataType:"json",
                    success:function(res){
                        console.log(res);
                        layer.alert('跳转成功');
                    }
                });
            });
        };
        if (obj.event === 'testing') {  // 监听实验中操作
            layer.confirm('确定将任务转换至试验中状态吗？', function (index) {
                $.ajax({
                    type:"post",
                    url:"/lims/updateToTesting",
                    data:JSON.stringify(data),
                    contentType:"application/json;charset=utf-8",
                    dataType:"json",
                    success:function(res){
                        console.log(res);
                        layer.alert('跳转成功');
                    }
                });
            });
        };
        if (obj.event === 'engineer') {  // 监听删除操作
            layer.confirm('确定将任务转换至工程师复核状态吗？', function (index) {
                $.ajax({
                    type:"post",
                    url:"/lims/updateToEngineer",
                    data:JSON.stringify(data),
                    contentType:"application/json;charset=utf-8",
                    dataType:"json",
                    success:function(res){
                        console.log(res);
                        layer.alert('跳转成功');
                    }
                });
            });
        };
        if (obj.event === 'report') {  // 监听删除操作
            layer.confirm('确定将任务转换至报告签发状态吗？', function (index) {
                $.ajax({
                    type:"post",
                    url:"/lims/updateToReport",
                    data:JSON.stringify(data),
                    contentType:"application/json;charset=utf-8",
                    dataType:"json",
                    success:function(res){
                        console.log(res);
                        layer.alert('跳转成功');
                    }
                });
            });
        };
        if (obj.event === 'taskend') {  // 监听删除操作
            layer.confirm('确定将任务取消吗？', function (index) {
                $.ajax({
                    type:"post",
                    url:"/lims/updateToTaskend",
                    data:JSON.stringify(data),
                    contentType:"application/json;charset=utf-8",
                    dataType:"json",
                    success:function(res){
                        console.log(res);
                        layer.alert('跳转成功');
                    }
                });
            });
        };





    });

    //监听表格复选框选择
    table.on('checkbox(currentTableFilter)', function (obj) {
        console.log(obj)
    });

    table.on('tool(currentTableFilter)', function (obj) {
        var data = obj.data;
        if (obj.event === 'edit') {

            var index = layer.open({
                title: '编辑用户',
                type: 2,
                shade: 0.2,
                maxmin:true,
                shadeClose: true,
                area: ['100%', '100%'],
                content: '../page/table/edit.html',
            });
            $(window).on("resize", function () {
                layer.full(index);
            });
            return false;
        } else if (obj.event === 'delete') {
            layer.confirm('真的删除行么', function (index) {
                obj.del();
                layer.close(index);
            });
        }
    });

});