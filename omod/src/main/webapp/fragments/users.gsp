<%
    ui.includeJavascript("cdrsync", "jquery.js")

    def id = config.id
%>
<%= ui.resourceLinks() %>

<table>
  <tr>
   <th>User Id</th>
   <th>Username</th>
  </tr>
  <% if (users) { %>
     <% users.each { %>
      <tr>
        <td>${ ui.format(it.userId) }</td>
        <td>${ ui.format(it.systemId) }</td>
      </tr>
    <% } %>
  <% } else { %>
  <tr>
    <td colspan="2">${ ui.message("general.none") }</td>
  </tr>
  <% } %>
</table>

<div style="color: blue; align-self: center">
    <button id="sync" style="color: red; align-self: center">Sync Data</button>
</div>

<script type="text/javascript">


    var jq = jQuery;

    jq(document).ready(function (){
        jq("#sync").click(function(){
            console.log("I am clicked");
            jq.ajax({
                url: "${ui.actionLink("getPatientsFromInitial")}",
                dataType: "json"
            }).success(function (){
                alert("I am clicked");
            })
        });


    });


</script>