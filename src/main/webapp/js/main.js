AUI().use('aui-base', 'aui-dialog', 'aui-io', 'liferay-portlet-url', 'aui-dialog-iframe',
    function (A) {
        A.all('.taskButton').each(function(btn) {
            console.log('button:'+btn);
            btn.on('click', function(){
                var taskId = this.getAttribute('data-id');
                processStep(taskId);
            });
        });

        A.all('.showTaskButton').each(function(btn) {
            console.log('show:'+btn);
            btn.on('click', function() {
                var taskId = this.getAttribute('data-id');
                var taskDefinitionKey = this.getAttribute('data-step');
                showTask(taskId, taskDefinitionKey);
            })
        });

        var newProcessButton = A.one('#newProcessButton');
        if(newProcessButton) {
            newProcessButton.on('click', function() {
                var processDefinitionId = getValueOfSelectedOption('#processselect');
                if(processDefinitionId) {
                    processStep(undefined, processDefinitionId);
                } else {
                    alert('No process selected');
                }
            });
        }

        var getValueOfSelectedOption = function(selectId) {
            var select = A.one(selectId);
            return select.get('options').item(select.get('selectedIndex')).getAttribute('value');
        }

        var processStep = function(taskId, processDefinitionId) {
            var url = Liferay.PortletURL.createRenderURL();
            url.setPortletId(liferayCamundaWorkflowPortletId);
            url.setWindowState("exclusive");
            url.setParameter("show", "Task");
            url.setParameter("taskId", taskId);
            url.setParameter("processDefinitionId", processDefinitionId);

            var popUpWindow = Liferay.Util.Window.getWindow({
                dialog: {
                    centered: true,
                    constrain2view: true,
                    cssClass: 'stepProcessor',
                    modal: true,
                    resizable: false,
                    width: 500
                }
            }).plug(A.Plugin.IO, {
                    uri: url.toString(),
                    autoLoad: false
                }
            ).render();

            popUpWindow.show();
            popUpWindow.titleNode.html("Task editieren");
            popUpWindow.io.start();
        }

        var showTask = function(taskId, taskDefinitionKey) {
            var url = Liferay.PortletURL.createResourceURL();
            url.setPortletId(liferayCamundaWorkflowPortletId);
            url.setParameter("taskId", taskId);
            url.setResourceId("getBpmn");

            var popUpWindow = Liferay.Util.Window.getWindow({
                dialog: {
                    centered: true,
                    constrain2view: true,
                    cssClass: 'showProcess',
                    modal: true,
                    resizable: false
                }
            }).render();

            popUpWindow.show();
            popUpWindow.titleNode.html("Prozessschritt");

            var BpmnViewer = window.BpmnJS;
            viewer = new BpmnViewer({ container: $('.showProcess .modal-body') });

            $.get(url, function(bpmn) {
                viewer.importXML(bpmn, function(err) {
                    if (!err) {
                        console.log('success!');
                        viewer.get('canvas').zoom('fit-viewport');

                        $('g[data-element-id='+taskDefinitionKey+']').css('fill','red')
                    } else {
                        console.log('something went wrong:', err);
                    }
                });
            }, 'text');
        }
    }
);
