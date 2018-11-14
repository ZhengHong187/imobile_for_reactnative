/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from 'react';
import {
  StyleSheet,
  Text,
  View,
  TouchableHighlight,
  Dimensions,
  Image
} from 'react-native';

import InnerListComponent from './SMInnerListComponent.js';

export default class OuterListItem extends Component {
  constructor(props){
    super(props);
    this.state={highLight:false,
                };
  }

  _onPress = ()=>{
    this.setState({highLight:!this.state.highLight});
  }
  render() {
    return (
      <View style={styles.container}>
        <TouchableHighlight style={styles.touchableContainer} onPress={this._onPress} underlayColor={'rgba(34,26,38,0.1)'}>
          <View style={styles.touchableSubView}>
            <Image style={styles.itemImage} source={this.props.Image}/>
            <Text style={styles.itemText}>{this.props.Text}</Text>
          </View>
        </TouchableHighlight>
        {this.state.highLight && <InnerListComponent index={this.props.Index} workspace={this.props.workspace}/>}
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor:'transparent',
  },
  touchableSubView: {
    backgroundColor: 'transparent',
    display: 'flex',
    flexDirection: 'row',
  },
  touchableContainer: {
    display: 'flex',
    flexDirection: 'row',
    justifyContent: 'flex-start',
    width: Dimensions.get('window').width,
    height: 50,
    backgroundColor:'transparent',
  },
  itemImage: {
    width:35,
    height:40,
    marginTop:5,
    marginBottom:5,
    marginLeft:25,
    backgroundColor:'transparent',
  },
  itemText: {
    marginLeft:10,
    lineHeight:50,
    backgroundColor:'transparent',
  }
});