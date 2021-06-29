$(document).ready(function (e1) {
    $.ajax({
        type: "GET",
        url: "modem/list",
        dataType: "json",
        success: function (data) {
            for (const key in data) {
                if (data.hasOwnProperty(key)) {
                    var item = data[key];
                    var active = !item.active ? 'icon-cross' : 'icon-check';
                    var cls = '';
                    cls += (item.active?' enable':' disable');
                    cls += (item.connected?' connected':' disconnected');

                    var service = $('<div class="service-item service-modem'+cls+'">\r\n'+
                        '<div class="service-label"></div>\r\n'+
                        '<div class="service-button">\r\n'+
                        '<button class="btn btn-sm btn-success connect">Connect</button>\r\n'+
                        '<button class="btn btn-sm btn-danger disconnect">Disonnect</button>\r\n'+
                        '</div>\r\n'+
                        '</div>');
                    service.attr('data-id', item.id);
                    service.find('.service-label').text(item.name+ ' via '+item.connectionType);

                    $('.service-wrapper').append(service);
                    $('.row-table tbody').append(
                        '<tr data-pk-id="' + item.id + '">\r\n' +
                        '	<td><input type="checkbox" class="check-all" name="id[]" value="' + item.id + '"></td>\r\n' +
                        '	<td><a href="modem-update.html?id=' + encodeURIComponent(item.id) + '">' + item.name + '</a></td>\r\n' +
                        '	<td><a href="modem-update.html?id=' + encodeURIComponent(item.id) + '">' + item.connectionType + '</a></td>\r\n' +
                        '	<td align="center"><span class="icon ' + active + '"></span></td>\r\n' +
                        '</tr>\r\n'
                    );
                }
            }
        }
    });

    $(document).on('click', '.service-wrapper .connect', function(e2){
        var modemID = $(this).closest('.service-item').attr('data-id');
        $.ajax({
            url:'api/device/',
            type:'POST',
            dataType:'jaosn',
            data:{action:'connect', id:modemID},
            success:function(data)
            {
                /**
                do nothing
                */
            }
        });
    });
    $(document).on('click', '.service-wrapper .disconnect', function(e2){
        var modemID = $(this).closest('.service-item').attr('data-id');
        $.ajax({
            url:'api/device/',
            type:'POST',
            dataType:'jaosn',
            data:{action:'disconnect', id:modemID},
            success:function(data)
            {
                /**
                do nothing
                */
            }
        });
    });
});

function updateModemUI(modemData)
{
    for(var i in modemData)
    {
        if(modemData.hasOwnProperty(i))
        {
            var id = i;
            $('.service-modem').filter('[data-id="'+id+'"]').removeClass('disconnected');
            $('.service-modem').filter('[data-id="'+id+'"]').removeClass('connected')
            if(modemData[i].connected)
            {
                $('.service-modem').filter('[data-id="'+id+'"]').addClass('connected');
            }
            else
            {
                $('.service-modem').filter('[data-id="'+id+'"]').addClass('disconnected');
            }
        }
    }   
}

function handleIncommingMessage(message) {
    var modemData = getModemData();
    updateModemUI(modemData);        
}